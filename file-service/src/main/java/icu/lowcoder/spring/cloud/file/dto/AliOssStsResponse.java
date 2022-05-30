package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AliOssStsResponse {
    private String securityToken;
    private String accessKeySecret;
    private String accessKeyId;
    private String expiration;
    // oss 配置
    private String bucket;
    private String endpoint;
}
