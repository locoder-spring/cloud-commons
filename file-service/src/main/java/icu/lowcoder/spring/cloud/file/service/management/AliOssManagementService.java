package icu.lowcoder.spring.cloud.file.service.management;

import icu.lowcoder.spring.cloud.file.dto.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/files/management/ali-oss")
public interface AliOssManagementService {

    @GetMapping("/buckets")
    List<AliOssBucket> getBuckets();

    @GetMapping("/buckets/{bucketName}/objects")
    AliOssListObjectPage getObjects(@PathVariable String bucketName, AliOssListObjectsParams params);

    @DeleteMapping("/buckets/{bucketName}/objects")
    List<String> delObjects(@PathVariable String bucketName, List<String> objectKeys);

    @PostMapping(value = "/buckets/{bucketName}/objects", params = "op=copy")
    List<String> copyObjects(@PathVariable String bucketName, AliOssCopyObjectsRequest request);

    /**
     * 创建 sts
     * @return
     */
    @PostMapping(value = "/sts")
    AliOssStsResponse applyAccessSts(String bucket);

    /**
     * 创建上传签名
     * @return
     */
    @PostMapping(value = "/sign")
    AliOssUploadPolicyResponse sign(String bucket, String dir);

    /**
     * 批量下载文件，打包成 zip，并存入临时目录
     * 返回打包后的文件信息
     *
     * @return FileDescription
     */
    @PostMapping(value = "/buckets/{bucketName}/objects", params = "op=batch-download")
    FileDescription batchDownload(@PathVariable String bucketName, AliOssBatchDownloadRequest request);
}
