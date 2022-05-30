package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AliOssListObjectsParams {
    private Integer maxSize = 100;
    private String prefix;
    private String continuationToken;
}
