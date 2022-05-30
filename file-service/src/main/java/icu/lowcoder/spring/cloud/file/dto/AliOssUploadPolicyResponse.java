package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class AliOssUploadPolicyResponse {
    private String accessId;
    private String policy;
    private String signature;
    private String dir;
    private String host;
    private Long expire;
    private String callback;

    private Map<String, String> customParams = new HashMap<>();
}
