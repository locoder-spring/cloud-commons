package icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat;

import lombok.Data;

@Data
public class WeChatApp {
    private String appId;
    private String secret;
    private String name;
}
