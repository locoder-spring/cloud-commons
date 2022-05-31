package icu.lowcoder.spring.cloud.bridge.wechat.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class JsApiSignRequest {
    @NotBlank
    private String appId;
    private String nonceStr;
    private String timestamp;
    private String url;
}
