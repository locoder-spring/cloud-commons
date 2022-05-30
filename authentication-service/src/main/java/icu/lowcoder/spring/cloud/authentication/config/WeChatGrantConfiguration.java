package icu.lowcoder.spring.cloud.authentication.config;

import icu.lowcoder.spring.commons.wechat.WeChatClient;
import icu.lowcoder.spring.cloud.authentication.dao.AccountRepository;
import icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat.WeChatGrantService;
import icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat.WeChatGrantServiceImpl;
import icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat.WeChatTokenGranter;
import icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat.WeChatWebTokenGranter;
import icu.lowcoder.spring.cloud.authentication.service.UserRegistrationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.OAuth2RequestValidator;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "icu.lowcoder.spring.cloud.auth.wechat", name = "enabled")
public class WeChatGrantConfiguration {

    @Bean
    public WeChatGrantService weChatGrantService(AccountRepository accountRepository, WeChatClient weChatClient, WeChatGrantProperties grantProperties) {
        return new WeChatGrantServiceImpl(accountRepository, weChatClient, grantProperties);
    }

    @Bean
    public WeChatTokenGranter weChatTokenGranter(
            WeChatGrantService weChatGrantService,
            @Qualifier("oauth2TokenService") AuthorizationServerTokenServices tokenService,
            ClientDetailsService clientDetailsService,
            OAuth2RequestFactory oAuth2RequestFactory,
            OAuth2RequestValidator oAuth2RequestValidator,
            UserRegistrationService userRegistrationService,
            AuthProperties authProperties
    ) {
        return new WeChatTokenGranter(weChatGrantService, tokenService, clientDetailsService, oAuth2RequestFactory, oAuth2RequestValidator, userRegistrationService, authProperties);
    }

    @Bean
    public WeChatWebTokenGranter weChatWebTokenGranter(
            WeChatGrantService weChatGrantService,
            @Qualifier("oauth2TokenService") AuthorizationServerTokenServices tokenService,
            ClientDetailsService clientDetailsService,
            OAuth2RequestFactory oAuth2RequestFactory,
            OAuth2RequestValidator oAuth2RequestValidator,
            UserRegistrationService userRegistrationService,
            AuthProperties authProperties
    ) {
        return new WeChatWebTokenGranter(weChatGrantService, tokenService, clientDetailsService, oAuth2RequestFactory, oAuth2RequestValidator, userRegistrationService, authProperties);
    }
}
