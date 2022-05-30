package icu.lowcoder.spring.cloud.file.service;

import icu.lowcoder.spring.cloud.file.dict.FileAcl;
import icu.lowcoder.spring.cloud.file.dto.FileDescription;
import icu.lowcoder.spring.cloud.file.dto.FileUploadRequest;
import icu.lowcoder.spring.cloud.file.dto.ObjectUUIDIdResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@RequestMapping("/files")
public interface FilesService {

    /**
     * 上传临时文件
     * 不能直接上传至具体的业务目录，需要先上传至临时目录，具体业务需要保存时再转存
     */
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    ObjectUUIDIdResponse upload(FileUploadRequest request);

    /**
     * 转存文件
     * @param ids 文件id列表
     * @param toPath 目标目录
     * @return 新的文件描述信息
     */
    @PutMapping("/{ids}")
    List<FileDescription> transfer(
            @PathVariable(value = "ids") UUID[] ids,
            @RequestParam(value = "toPath") String toPath,
            @RequestParam(value = "acl", required = false, defaultValue = "DEFAULT") FileAcl acl
    );

    /**
     * 获取文件描述
     * @param id 文件id
     */
    @GetMapping(value = "/{id}", params = "desc")
    FileDescription getDesc(@PathVariable(value = "id") UUID id);

    /**
     * 下载文件
     * @param id 文件id
     */
    @GetMapping(value = "/{id}", params = "byte")
    void getByte(@PathVariable(value = "id") UUID id, HttpServletResponse response);

}
