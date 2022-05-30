package icu.lowcoder.spring.cloud.authentication.config;

import icu.lowcoder.spring.cloud.authentication.util.RandomSmsCodeGenerator;
import icu.lowcoder.spring.cloud.authentication.oauth2.provider.sms.RedisSmsCodeGrantService;
import icu.lowcoder.spring.cloud.authentication.oauth2.provider.sms.SmsCodeGrantService;
import icu.lowcoder.spring.cloud.authentication.oauth2.provider.sms.SmsCodeTokenGranter;
import icu.lowcoder.spring.cloud.authentication.service.UserRegistrationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.OAuth2RequestValidator;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "icu.lowcoder.spring.cloud.auth.sms", name = "enabled")
public class SmsGrantConfiguration {

    @Bean
    public RandomSmsCodeGenerator smsCodeGenerator(SmsGrantProperties smsGrantProperties) {
        return new RandomSmsCodeGenerator(smsGrantProperties.getRandomCodeLength());
    }

    @Bean
    public SmsCodeGrantService smsCodeServices(
            RandomSmsCodeGenerator smsCodeGenerator,
            SmsGrantProperties smsGrantProperties,
            RedisConnectionFactory redisConnectionFactory) {

        return new RedisSmsCodeGrantService(redisConnectionFactory, smsCodeGenerator, smsGrantProperties.getCacheKey(), smsGrantProperties.getExpireInSeconds());
    }

    @Bean
    public SmsCodeTokenGranter smsCodeTokenGranter(
            @Qualifier("oauth2UserDetailsService")
            UserDetailsService userDetailsService,
            SmsCodeGrantService smsCodeServices,
            @Qualifier("oauth2TokenService") AuthorizationServerTokenServices tokenService,
            ClientDetailsService clientDetailsService,
            OAuth2RequestFactory oAuth2RequestFactory,
            OAuth2RequestValidator oAuth2RequestValidator,
            UserRegistrationService userRegistrationService,
            AuthProperties authProperties
    ) {
        // sms
        return new SmsCodeTokenGranter(userDetailsService, userRegistrationService, smsCodeServices, tokenService, clientDetailsService, oAuth2RequestFactory, oAuth2RequestValidator, authProperties);
    }
}
