package icu.lowcoder.spring.cloud.file.dict;

import lombok.Getter;

/**
 * 描述文件访问权限
 * aliOssAcl 描述阿里云对应 x-oss-object-acl 的值
 */
@Getter
public enum FileAcl {
    DEFAULT("默认", "default"),
    PRIVATE("私有资源", "private"),
    PUBLIC_READ("公共读", "public-read"),
    PUBLIC_READ_WRITE("公共读", "public-read-write"),
    ;

    private String description;
    private String aliOssAcl;

    FileAcl(String description, String aliOssAcl) {
        this.description = description;
        this.aliOssAcl = aliOssAcl;
    }
}
