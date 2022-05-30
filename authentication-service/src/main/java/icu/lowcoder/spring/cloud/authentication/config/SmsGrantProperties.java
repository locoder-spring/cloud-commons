package icu.lowcoder.spring.cloud.authentication.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "icu.lowcoder.spring.cloud.auth.sms")
public class SmsGrantProperties {
    private Boolean enabled = false;
    private Integer randomCodeLength = 6;
    private String cacheKey = "icu.lowcoder.spring.cloud.auth.sms.cache";
    private Long expireInSeconds = 60L;
}
