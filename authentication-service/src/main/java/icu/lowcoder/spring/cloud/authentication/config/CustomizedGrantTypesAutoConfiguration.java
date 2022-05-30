package icu.lowcoder.spring.cloud.authentication.config;

import icu.lowcoder.spring.cloud.authentication.auth.UserDetailsByNameOrIdServiceWrapper;
import icu.lowcoder.spring.cloud.authentication.oauth2.provider.token.AutoHandleGetFailedRedisTokenStore;
import icu.lowcoder.spring.cloud.authentication.service.UserDetailsByIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.OAuth2RequestValidator;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestValidator;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

import java.util.Collections;

@AutoConfigureBefore(OAuthServerConfiguration.class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({SmsGrantProperties.class, WeChatGrantProperties.class})
@Import({SmsGrantConfiguration.class, WeChatGrantConfiguration.class})
public class CustomizedGrantTypesAutoConfiguration {

    @Bean
    public OAuth2RequestValidator oAuth2RequestValidator() {
        return new DefaultOAuth2RequestValidator();
    }

    @Bean
    public OAuth2RequestFactory requestFactory(ClientDetailsService clientDetailsService) {
        return new DefaultOAuth2RequestFactory(clientDetailsService);
    }

    @Bean("oauth2RedisTokenStore")
    public TokenStore redisTokenStore(RedisConnectionFactory redisConnectionFactory,
                                      @Autowired(required = false) AuthProperties authProperties) {
        RedisTokenStore redisTokenStore = new AutoHandleGetFailedRedisTokenStore(redisConnectionFactory);
        if (authProperties != null) {
            redisTokenStore.setPrefix(authProperties.getTokenStoreKeyPrefix());
        }

        return redisTokenStore;
    }

    @Bean("oauth2ProviderManager")
    public ProviderManager providerManager(@Qualifier("oauth2UserDetailsService") UserDetailsService userDetailsService,
                                           UserDetailsByIdService userDetailsByIdService) {
        PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
        provider.setPreAuthenticatedUserDetailsService(new UserDetailsByNameOrIdServiceWrapper<>(userDetailsByIdService, userDetailsService));

        return new ProviderManager(Collections.singletonList(provider));
    }

    @Bean("oauth2TokenService")
    public DefaultTokenServices tokenServices(@Qualifier("oauth2RedisTokenStore") TokenStore tokenStore,
                                              @Qualifier("oauth2ProviderManager") ProviderManager providerManager) {

        DefaultTokenServices tokenServices = new DefaultTokenServices();

        tokenServices.setTokenStore(tokenStore);
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setAuthenticationManager(providerManager);

        return tokenServices;
    }
}
