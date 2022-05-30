package icu.lowcoder.spring.cloud.authentication.config;

import icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat.WeChatApp;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "icu.lowcoder.spring.cloud.auth.wechat")
public class WeChatGrantProperties {
    private Boolean enabled = false;
    private List<WeChatApp> apps = new ArrayList<>();
}
