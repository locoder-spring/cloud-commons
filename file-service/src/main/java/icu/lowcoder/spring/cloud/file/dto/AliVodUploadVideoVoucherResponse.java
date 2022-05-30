package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class AliVodUploadVideoVoucherResponse {
    private String requestId;
    private String videoId;
    private String uploadAddress;
    private String uploadAuth;
}
