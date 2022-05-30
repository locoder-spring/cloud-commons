package icu.lowcoder.spring.cloud.file.controller;

import com.aliyun.oss.model.OSSObject;
import icu.lowcoder.spring.commons.ali.oss.AliOssClient;
import icu.lowcoder.spring.commons.ali.oss.model.CallbackBody;
import icu.lowcoder.spring.commons.ali.oss.model.OssAccessSts;
import icu.lowcoder.spring.commons.ali.oss.model.UploadPolicy;
import icu.lowcoder.spring.commons.ali.oss.sts.AliOssStsManager;
import icu.lowcoder.spring.commons.security.SecurityUtils;
import icu.lowcoder.spring.commons.util.json.JsonUtils;
import icu.lowcoder.spring.commons.util.spring.BeanUtils;
import icu.lowcoder.spring.cloud.file.dao.FileUploadRecordRepository;
import icu.lowcoder.spring.cloud.file.dto.AliOssCallbackResponse;
import icu.lowcoder.spring.cloud.file.dto.AliOssStsResponse;
import icu.lowcoder.spring.cloud.file.dto.AliOssUploadPolicyResponse;
import icu.lowcoder.spring.cloud.file.dto.FileDescription;
import icu.lowcoder.spring.cloud.file.entity.FileUploadRecord;
import icu.lowcoder.spring.cloud.file.manager.AliOssBackendFilesManager;
import icu.lowcoder.spring.cloud.file.service.AliOssFilesService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
public class AliOssFilesController implements AliOssFilesService {

    private final AliOssStsManager aliOssStsManager;
    private final AliOssClient aliOssClient;
    private final FileUploadRecordRepository fileUploadRecordRepository;
    private final AliOssBackendFilesManager aliOssBackendFilesManager;

    public AliOssFilesController(@Autowired(required = false) AliOssStsManager aliOssStsManager, AliOssClient aliOssClient, FileUploadRecordRepository fileUploadRecordRepository, AliOssBackendFilesManager aliOssBackendFilesManager) {
        this.aliOssStsManager = aliOssStsManager;
        this.aliOssClient = aliOssClient;
        this.fileUploadRecordRepository = fileUploadRecordRepository;
        this.aliOssBackendFilesManager = aliOssBackendFilesManager;
    }

    @Override
    public AliOssStsResponse applyAccessSts() {
        if (aliOssStsManager == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "未配置STS或配置错误");
        }

        OssAccessSts sts = aliOssStsManager.applySts(SecurityUtils.getPrincipalId(false));

        return BeanUtils.instantiate(AliOssStsResponse.class, sts);
    }

    @Override
    public AliOssUploadPolicyResponse sign() {
        try {
            UploadPolicy uploadPolicy = aliOssClient.uploadPolicy(null, SecurityUtils.getPrincipalId());
            return BeanUtils.instantiate(AliOssUploadPolicyResponse.class, uploadPolicy);
        } catch (Exception e) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "阿里云Oss上传签名异常");
        }
    }

    @Override
    @SneakyThrows
    public void download(String object, HttpServletResponse response) {
        OSSObject ossObject = aliOssClient.getObject(object);

        InputStream inputStream = ossObject.getObjectContent();
        response.addHeader("Content-Disposition", ossObject.getObjectMetadata().getContentDisposition());
        response.addHeader("Content-Length", "" + ossObject.getObjectMetadata().getContentLength());
        response.setContentType(ossObject.getObjectMetadata().getContentType());

        OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
        FileCopyUtils.copy(inputStream, outputStream);
    }

    @Override
    @SneakyThrows
    @Transactional
    public FileDescription upload(@RequestPart(value = "file") MultipartFile file, String dir) {
        String name = String.format("%s.%s", UUID.randomUUID(), StringUtils.getFilenameExtension(file.getOriginalFilename()));
        return aliOssBackendFilesManager.upload(file, dir, name);
    }

    @Override
    @SneakyThrows
    @Transactional
    public AliOssCallbackResponse callback(@RequestHeader("Authorization") String authorization,
                                           @RequestHeader("x-oss-pub-key-url") String pubKeyUrl,
                                           HttpServletRequest request) {
        String bodyStr = extractBodyString(request);
        Map<String, String[]> bodyParams = request.getParameterMap();
        Map<String, Object> paramsMap = bodyParams.entrySet().stream().map(entry -> new HashMap.SimpleEntry<>(entry.getKey(), entry.getValue().length > 1 ? entry.getValue() : (entry.getValue().length == 0 ? "" : entry.getValue()[0]))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String bodyJsonStr = JsonUtils.toJson(paramsMap);
        String requestUri = request.getRequestURI() ;
        String requestQuery = request.getQueryString();

        if (log.isDebugEnabled()) {
            StringBuilder headers = new StringBuilder();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.append("= ").append(headerName).append(": ").append(request.getHeader(headerName)).append("\n");
            }

            String logSB =
                    "\n===== received callback ..." + "\n" +
                            "= bodyStr: " + bodyStr + "\n" +
                            "= bodyJson: " + bodyJsonStr + "\n" +
                            "= uri: " + requestUri + "\n" +
                            "= query: " + requestQuery + "\n" +
                            "= authorization: " + authorization + "\n" +
                            "= pubKeyUrl: " + pubKeyUrl + "\n" +
                            "========== headers =========\n" + headers +
                            "===== received callback ===== " + "\n";
            log.debug(logSB);
        }

        if(aliOssClient.verifyCallbackRequest(requestUri, requestQuery, authorization, pubKeyUrl, bodyStr)) {
            CallbackBody callback = JsonUtils.parse(bodyJsonStr, CallbackBody.class);
            FileUploadRecord uploadRecord = new FileUploadRecord();
            uploadRecord.setExtension(StringUtils.getFilenameExtension(callback.getOriginalName()));

            String object = callback.getObject();
            String path = object.contains("/") ? object.substring(0, object.lastIndexOf("/")) : "";
            String name = object.contains("/") ? object.substring(object.lastIndexOf("/") + 1) : object;

            //uploadRecord.setId(UUID.randomUUID());
            uploadRecord.setName(name);
            uploadRecord.setOriginalFilename(URLDecoder.decode(callback.getOriginalName(), "UTF-8"));
            uploadRecord.setPath(path);
            uploadRecord.setSize(callback.getSize());
            uploadRecord.setType(callback.getMimeType());
            uploadRecord.setUserId(callback.getUserId());
            uploadRecord.setCreatedUser(callback.getUserId());
            uploadRecord.setCreatedTime(new Date());
            uploadRecord.setBucket(callback.getBucket());
            uploadRecord.setFullKey(callback.getObject());
            fileUploadRecordRepository.save(uploadRecord);

            return new AliOssCallbackResponse(uploadRecord.getId());
        } else {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "verify not pass.");
        }
    }

    private String extractBodyString(HttpServletRequest request) {
        String requestEncoding = request.getCharacterEncoding();
        if (requestEncoding == null) {
            requestEncoding = "utf-8";
        }
        String finalRequestEncoding = requestEncoding;

        Map<String, String[]> form = request.getParameterMap();
        return form.entrySet().stream()
                .map(entry -> Stream.of(entry.getValue())
                        .map(value ->
                                {
                                    try {
                                        return URLEncoder.encode(entry.getKey(), finalRequestEncoding).replaceAll("\\+", "%20") + "=" +
                                        URLEncoder.encode(value, finalRequestEncoding).replaceAll("\\+", "%20");
                                    } catch (UnsupportedEncodingException e) {
                                        throw new IllegalStateException("Failed to extract body string", e);
                                    }
                                }
                        )
                        .collect(Collectors.joining("&"))
                )
                .collect(Collectors.joining("&"));
    }
}
