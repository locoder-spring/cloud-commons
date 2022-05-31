package icu.lowcoder.spring.cloud.bridge.wechat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "icu.lowcoder.spring.cloud.bridge.wechat")
public class WeChatBridgeProperties {
    private List<WeChatApp> apps = new ArrayList<>();
    private String callbackUrl = "https://api-host/v3/bridge/we-chat/authorize/callback";
    private String authorizeUrl = "https://api-host/v3/bridge/we-chat/authorize";
    private String authorizeCacheKeyPrefix = "icu.lowcoder.spring.cloud.bridge.wechat.authorize.cache";
    private String authorizeCookieKey = "locoder_spring_wechat_bridge_auth_key";

    @Getter
    @Setter
    public static class WeChatApp {
        private String appId;
        private String secret;
        private String name;
    }
}
