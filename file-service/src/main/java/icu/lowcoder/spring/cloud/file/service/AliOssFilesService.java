package icu.lowcoder.spring.cloud.file.service;

import icu.lowcoder.spring.cloud.file.dto.AliOssCallbackResponse;
import icu.lowcoder.spring.cloud.file.dto.AliOssStsResponse;
import icu.lowcoder.spring.cloud.file.dto.AliOssUploadPolicyResponse;
import icu.lowcoder.spring.cloud.file.dto.FileDescription;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequestMapping("/files/ali-oss")
public interface AliOssFilesService {

    /**
     * 创建 sts
     * @return
     */
    @PostMapping(value = "/sts")
    AliOssStsResponse applyAccessSts();

    /**
     * 创建上传签名
     * @return
     */
    @PostMapping(value = "/sign")
    AliOssUploadPolicyResponse sign();

    /**
     * 下载文件
     * @param object
     * @param response
     */
    @GetMapping(params = "op=download")
    void download(String object, HttpServletResponse response);

    /**
     * 上传文件
     * @param file 文件
     * @param dir 上传目录
     */
    @PostMapping(params = "op=upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    FileDescription upload(@RequestPart(value = "file") MultipartFile file, String dir);

    /**
     * 回调
     */
    @PostMapping("/callback")
    AliOssCallbackResponse callback(@RequestHeader("Authorization") String authorization,
                                    @RequestHeader("x-oss-pub-key-url") String pubKeyUrl,
                                    HttpServletRequest request);

}
