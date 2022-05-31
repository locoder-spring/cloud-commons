package icu.lowcoder.spring.cloud.bridge.wechat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizeParams {
    private String appId;
    private String redirectUrl;
    private String scope;
    private String state;
}
