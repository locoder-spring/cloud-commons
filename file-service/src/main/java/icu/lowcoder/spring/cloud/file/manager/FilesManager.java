package icu.lowcoder.spring.cloud.file.manager;

import icu.lowcoder.spring.cloud.file.dict.FileAcl;
import icu.lowcoder.spring.cloud.file.dto.FileDescription;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface FilesManager {
    /**
     * 上传文件
     * @param multipartFile 文件
     * @param path 保存路径; 留空则使用默认目录或临时目录
     * @param name 文件名
     * @return
     */
    FileDescription upload(MultipartFile multipartFile, String path, String name);

    /**
     * 获取文件描述
     * @param id 文件id
     * @return
     */
    FileDescription getDesc(UUID id);

    /**
     * 获取文件流
     * @param id 文件id
     * @return
     */
    InputStream getInputStream(UUID id);

    /**
     * 文件移动到其他路径
     * @param ids 文件id数组
     * @param path 新的路径
     * @return
     */
    List<FileDescription> transfer(UUID[] ids, String path);

    /**
     * 文件移动到其他路径并设置权限
     * @param ids 文件id数组
     * @param path 新的路径
     * @param acl 访问权限
     * @return 返回新的文件描述信息
     */
    List<FileDescription> transfer(UUID[] ids, String path, FileAcl acl);

}
