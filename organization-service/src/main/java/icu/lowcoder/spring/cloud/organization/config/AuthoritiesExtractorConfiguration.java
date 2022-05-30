package icu.lowcoder.spring.cloud.organization.config;

import icu.lowcoder.spring.cloud.organization.dto.AuthorityModel;
import icu.lowcoder.spring.cloud.organization.manager.CacheableEmployeeAuthoritiesManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class AuthoritiesExtractorConfiguration {

    @Bean
    public AuthoritiesExtractor principalExtractor(CacheableEmployeeAuthoritiesManager employeeAuthoritiesManager) {
        return new EmployeeAuthoritiesExtractor(employeeAuthoritiesManager);
    }

    public static class EmployeeAuthoritiesExtractor extends FixedAuthoritiesExtractor {

        private final CacheableEmployeeAuthoritiesManager employeeAuthoritiesManager;

        private final static String PRINCIPAL_ID_KEY = "id";


        public EmployeeAuthoritiesExtractor(CacheableEmployeeAuthoritiesManager employeeAuthoritiesManager) {
            this.employeeAuthoritiesManager = employeeAuthoritiesManager;
        }

        @Override
        public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
            List<GrantedAuthority> authorities = super.extractAuthorities(map);

            if (map != null && map.containsKey(PRINCIPAL_ID_KEY)) {
                UUID accountId = null;
                Object idObj = map.get(PRINCIPAL_ID_KEY);
                if (idObj instanceof String) {
                    try {
                        accountId = UUID.fromString((String) idObj);
                    } catch (Exception ignored) {
                    }
                } else if (idObj instanceof UUID) {
                    accountId = (UUID) idObj;
                }

                if (accountId != null) {
                    try {
                        authorities.addAll(
                                employeeAuthoritiesManager.getEmployeeAuthorities(accountId)
                                        .stream()
                                        .map(AuthorityModel::getCode)
                                        .distinct()
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList())
                        );
                    } catch (Exception e) {
                        log.warn("Extract employee authorities error: {}", e.getMessage(), e);
                    }

                }
            }

            return authorities;
        }
    }

}
