package icu.lowcoder.spring.cloud.message.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "icu.lowcoder.spring.cloud.message.push")
public class PushProperties {
    private WeChatPushProperties weChat = new WeChatPushProperties();
    private EMailProperties email = new EMailProperties();

    @Getter
    @Setter
    public static class WeChatPushProperties {
        private List<WeChatApp> apps = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class WeChatApp {
        private String name = "";
        private String appId;
        private String secret;
    }

    @Getter
    @Setter
    public static class EMailProperties {
        private String defaultFromName = "易电";
        private String from;
    }
}
