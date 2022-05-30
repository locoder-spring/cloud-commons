package icu.lowcoder.spring.cloud.authentication.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class WeChatBindPhoneRequest {
    @NotBlank
    private String encryptedData;
    @NotBlank
    private String iv;
}
