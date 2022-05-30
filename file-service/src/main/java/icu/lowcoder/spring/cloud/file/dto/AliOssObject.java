package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AliOssObject {
    private String key;
    private String fullKey;
    private Long size;
    private String storageClass;
    private Date lastModified;
    private Boolean isCommonPrefix;
}
