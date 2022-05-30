package icu.lowcoder.spring.cloud.authentication.oauth2.provider.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

@Slf4j
public class AutoHandleGetFailedRedisTokenStore extends RedisTokenStore {

    public AutoHandleGetFailedRedisTokenStore(RedisConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        OAuth2AccessToken oAuth2AccessToken = null;
        try {
            oAuth2AccessToken = super.getAccessToken(authentication);
        } catch (Exception e) {
            log.warn("{}, return null", e.getMessage());
        }
        return oAuth2AccessToken;
    }
}
