package icu.lowcoder.spring.cloud.file.controller.management;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import icu.lowcoder.spring.commons.ali.oss.AliOssClient;
import icu.lowcoder.spring.commons.ali.oss.AliOssProperties;
import icu.lowcoder.spring.commons.ali.oss.model.OssAccessSts;
import icu.lowcoder.spring.commons.ali.oss.model.UploadPolicy;
import icu.lowcoder.spring.commons.ali.oss.sts.AliOssStsManager;
import icu.lowcoder.spring.commons.security.SecurityUtils;
import icu.lowcoder.spring.commons.util.spring.BeanUtils;
import icu.lowcoder.spring.cloud.file.dao.FileUploadRecordRepository;
import icu.lowcoder.spring.cloud.file.dto.*;
import icu.lowcoder.spring.cloud.file.entity.FileUploadRecord;
import icu.lowcoder.spring.cloud.file.service.management.AliOssManagementService;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RestController
public class AliOssManagementController implements AliOssManagementService {

    @Autowired(required = false)
    private OSS oss;
    @Autowired(required = false)
    private AliOssStsManager aliOssStsManager;
    @Autowired(required = false)
    private AliOssClient aliOssClient;
    @Autowired(required = false)
    private AliOssProperties aliOssProperties;
    @Autowired
    private FileUploadRecordRepository fileUploadRecordRepository;

    @Override
    public AliOssStsResponse applyAccessSts(String bucket) {
        if (aliOssStsManager == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "未配置STS或配置错误");
        }
        if (bucket == null) {
            throw new HttpServerErrorException(HttpStatus.BAD_REQUEST, "需要指定Bucket");
        }

        OssAccessSts sts = aliOssStsManager.applySts(bucket, SecurityUtils.getPrincipalId());

        return BeanUtils.instantiate(AliOssStsResponse.class, sts);
    }

    @Override
    public AliOssUploadPolicyResponse sign(String bucket, @RequestParam(required = false) String dir) {
        if (bucket == null) {
            throw new HttpServerErrorException(HttpStatus.BAD_REQUEST, "需要指定Bucket");
        }

        try {
            UploadPolicy uploadPolicy = aliOssClient.uploadPolicy(bucket, dir, SecurityUtils.getPrincipalId());
            return BeanUtils.instantiate(AliOssUploadPolicyResponse.class, uploadPolicy);
        } catch (Exception e) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "阿里云Oss上传签名异常");
        }
    }

    @Override
    @Transactional
    public FileDescription batchDownload(@PathVariable String bucketName, @Valid @RequestBody AliOssBatchDownloadRequest request) {
        // bucket 判断
        OSS client = getClient();
        checkBucketStatus(bucketName);

        // temp dir
        String tmpDir = "temp/";
        if (aliOssProperties != null) {
            tmpDir = aliOssProperties.getDefDir();
        }

        try {
            File tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".lowcoder.spring.tmp");

            // download and archive
            @Cleanup ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(tmpFile));

            List<String> errorList = new ArrayList<>();
            for (AliOssBatchDownloadRequest.BatchOpFile file : request.getFiles()) {
                if (client.doesObjectExist(bucketName, file.getPath())) {
                    OSSObject object = client.getObject(bucketName, file.getPath());
                    outputStream.putNextEntry(new ZipEntry(file.getName()));

                    @Cleanup InputStream objectInputStream = object.getObjectContent();
                    StreamUtils.copy(objectInputStream, outputStream);
                    objectInputStream.close();
                } else {
                    // add to error list
                    errorList.add(String.format("%s,%s:%s;", file.getPath(), file.getName(), "oss result object not exist"));
                }
            }

            if (!errorList.isEmpty()) {
                outputStream.putNextEntry(new ZipEntry("error_list.txt"));
                outputStream.write(StringUtils.collectionToDelimitedString(errorList, ";\n").getBytes());
                outputStream.flush();
            }

            outputStream.finish();
            outputStream.close();

            // upload
            String fileName = String.format("%s-%s.zip", "批量下载", UUID.randomUUID());
            String objectName = String.format("%s/%s", tmpDir, fileName);
            client.putObject(bucketName, objectName, tmpFile);

            // 本地记录
            FileUploadRecord newRecord = new FileUploadRecord();
            newRecord.setPath(objectName);
            newRecord.setExtension(org.springframework.util.StringUtils.getFilenameExtension(fileName));
            newRecord.setName(fileName);
            newRecord.setOriginalFilename(fileName);
            newRecord.setSize(tmpFile.length());
            newRecord.setType(Files.probeContentType(tmpFile.toPath()));
            newRecord.setUserId(SecurityUtils.getPrincipalId());
            fileUploadRecordRepository.save(newRecord);

            // 返回
            FileDescription fd = new FileDescription();
            fd.setPath(objectName);
            fd.setId(newRecord.getId());
            fd.setSize(newRecord.getSize());
            fd.setName(newRecord.getOriginalFilename());

            return fd;
        } catch (Exception e) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "合并下载文件失败");
        }
    }

    @Override
    public List<AliOssBucket> getBuckets() {
        OSS client = getClient();
        return client.listBuckets().stream()
                .map(b -> {
                    AliOssBucket bucket = new AliOssBucket();
                    bucket.setName(b.getName());
                    bucket.setCreateDate(b.getCreationDate());
                    bucket.setLocation(b.getLocation());
                    bucket.setExtranetEndpoint(b.getExtranetEndpoint());
                    bucket.setIntranetEndpoint(b.getIntranetEndpoint());
                    return bucket;
                })
                .collect(Collectors.toList());
    }

    @Override
    public AliOssListObjectPage getObjects(@PathVariable String bucketName, @Valid AliOssListObjectsParams params) {
        AliOssListObjectPage page = new AliOssListObjectPage();

        // bucket 判断
        OSS client = getClient();
        checkBucketStatus(bucketName);

        // 列举文件
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request(bucketName);
        listObjectsV2Request.setDelimiter("/");
        if (StringUtils.hasText(params.getPrefix())) {
            listObjectsV2Request.setPrefix(params.getPrefix().trim());
        }

        // 分页
        listObjectsV2Request.setMaxKeys(params.getMaxSize());
        if (StringUtils.hasText(params.getContinuationToken())) {
            listObjectsV2Request.setContinuationToken(params.getContinuationToken().trim());
        }

        ListObjectsV2Result result;
        try {
            result = client.listObjectsV2(listObjectsV2Request);
        } catch (OSSException e) {
            if(e.getErrorCode() != null && e.getErrorCode().equals("AccessDenied")) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "没有访问权限");
            }
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "访问bucket文件异常");
        }
        String currentDir = result.getPrefix();
        if (currentDir != null && !currentDir.endsWith("/")) {
            currentDir = currentDir.replaceAll("(.*/).*", "$1");
        }

        page.setMaxKeys(params.getMaxSize());
        page.setContinuationToken(result.getContinuationToken());
        page.setNextContinuationToken(result.getNextContinuationToken());

        for (String commonPrefix : result.getCommonPrefixes()) {
            AliOssObject ossObject = new AliOssObject();
            ossObject.setIsCommonPrefix(true);
            ossObject.setFullKey(commonPrefix);
            ossObject.setKey(currentDir == null ? commonPrefix : commonPrefix.replaceFirst(currentDir, ""));

            page.getObjects().add(ossObject);
        }

        for (OSSObjectSummary objectSummary : result.getObjectSummaries()) {
            AliOssObject ossObject = new AliOssObject();
            ossObject.setIsCommonPrefix(false);
            ossObject.setSize(objectSummary.getSize());
            ossObject.setFullKey(objectSummary.getKey());
            ossObject.setKey(currentDir == null ? objectSummary.getKey() : objectSummary.getKey().replaceFirst(currentDir, ""));
            ossObject.setStorageClass(objectSummary.getStorageClass());
            ossObject.setLastModified(objectSummary.getLastModified());

            page.getObjects().add(ossObject);
        }

        return page;
    }

    @Override
    public List<String> delObjects(@PathVariable String bucketName, @RequestBody List<String> objectKeys) {
        // bucket 判断
        OSS client = getClient();
        checkBucketStatus(bucketName);

        if (objectKeys.size() > 1000) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "批量删除每次最多1000个文件");
        }

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName)
                .withKeys(objectKeys)
                .withQuiet(true);

        DeleteObjectsResult deleteObjectsResult = client.deleteObjects(deleteObjectsRequest);
        // return failed objects
        return deleteObjectsResult.getDeletedObjects();
    }

    @Override
    public List<String> copyObjects(@PathVariable String bucketName, @RequestBody @Valid AliOssCopyObjectsRequest request) {
        // bucket 判断
        OSS client = getClient();
        checkBucketStatus(bucketName);

        List<String> failedKeys = new ArrayList<>();
        request.getObjects().forEach(copy -> {
            if (client.doesObjectExist(bucketName, copy.getSourceKey())) {
                ObjectMetadata objectMetadata = client.getObjectMetadata(bucketName, copy.getSourceKey());
                if (objectMetadata.getContentLength() >= 8589934592L) {
                    try {
                        // 分片
                        long contentLength = objectMetadata.getContentLength();
                        long partSize = 1024 * 1024 * 10;
                        // 计算分片总数。
                        int partCount = (int) (contentLength / partSize);
                        if (contentLength % partSize != 0) {
                            partCount++;
                        }
                        // 初始化拷贝任务。可以通过InitiateMultipartUploadRequest指定目标文件元信息。
                        InitiateMultipartUploadRequest initiateMultipartUploadRequest = new InitiateMultipartUploadRequest(bucketName, copy.getTargetKey());
                        InitiateMultipartUploadResult initiateMultipartUploadResult = client.initiateMultipartUpload(initiateMultipartUploadRequest);
                        String uploadId = initiateMultipartUploadResult.getUploadId();
                        List<PartETag> partETags = new ArrayList<>();
                        for (int i = 0; i < partCount; i++) {
                            // 计算每个分片的大小。
                            long skipBytes = partSize * i;
                            long size = Math.min(partSize, contentLength - skipBytes);

                            // 创建UploadPartCopyRequest。可以通过UploadPartCopyRequest指定限定条件。
                            UploadPartCopyRequest uploadPartCopyRequest =
                                    new UploadPartCopyRequest(bucketName, copy.getSourceKey(), bucketName, copy.getTargetKey());
                            uploadPartCopyRequest.setUploadId(uploadId);
                            uploadPartCopyRequest.setPartSize(size);
                            uploadPartCopyRequest.setBeginIndex(skipBytes);
                            uploadPartCopyRequest.setPartNumber(i + 1);
                            UploadPartCopyResult uploadPartCopyResult = client.uploadPartCopy(uploadPartCopyRequest);

                            // 将返回的分片ETag保存到partETags中。
                            partETags.add(uploadPartCopyResult.getPartETag());
                        }

                        // 提交分片拷贝任务。
                        CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(bucketName, copy.getTargetKey(), uploadId, partETags);
                        client.completeMultipartUpload(completeMultipartUploadRequest);
                    } catch (Exception e) {
                        log.debug("Ali oss copy object failed [{} -> {}]", copy.getSourceKey(), copy.getTargetKey(), e);
                        failedKeys.add(copy.getSourceKey());
                    }
                } else {
                    // 简单拷贝
                    try {
                        client.copyObject(bucketName, copy.getSourceKey(), bucketName, copy.getTargetKey());
                    } catch (Exception e) {
                        log.debug("Ali oss copy object failed [{} -> {}]", copy.getSourceKey(), copy.getTargetKey(), e);
                        failedKeys.add(copy.getSourceKey());
                    }
                }
            } else {
                failedKeys.add(copy.getSourceKey());
            }
        });

        return failedKeys;
    }

    private OSS getClient() {
        if (oss == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "未配置AliOss");
        }

        return oss;
    }

    private void checkBucketStatus(String bucketName) {
        if (!getClient().doesBucketExist(bucketName)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "bucket不存在");
        }
    }
}
