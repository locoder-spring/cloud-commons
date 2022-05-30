package icu.lowcoder.spring.cloud.authentication.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = "icu.lowcoder.spring.cloud.auth")
public class AuthProperties {
    /**
     * token store key prefix, default value is 'icu.lowcoder.spring.cloud.auth:'
     */
    private String tokenStoreKeyPrefix = "icu.lowcoder.spring.cloud.auth:";

    private Integer minPasswordLength = 8;

    private Set<String> autoRegisterClients = new HashSet<>();
    private Set<String> allowReplacePhoneClients = new HashSet<>();
}
