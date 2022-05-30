package icu.lowcoder.spring.cloud.content.feign.model;

import lombok.Getter;

/**
 * 描述文件访问权限
 */
@Getter
public enum FileAcl {
    DEFAULT("默认"),
    PRIVATE("私有资源"),
    PUBLIC_READ("公共读"),
    PUBLIC_READ_WRITE("公共读"),
    ;

    private String description;

    FileAcl(String description) {
        this.description = description;
    }
}
