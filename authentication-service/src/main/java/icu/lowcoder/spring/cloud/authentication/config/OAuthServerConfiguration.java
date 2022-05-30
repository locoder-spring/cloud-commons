package icu.lowcoder.spring.cloud.authentication.config;

import icu.lowcoder.spring.commons.security.oauth2.AccountAuthenticationConverter;
import icu.lowcoder.spring.commons.security.oauth2.CustomizedAccessTokenConverter;
import icu.lowcoder.spring.cloud.authentication.oauth2.provider.sms.SmsCodeTokenGranter;
import icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat.WeChatTokenGranter;
import icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat.WeChatWebTokenGranter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenGranter;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenGranter;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableAuthorizationServer
public class OAuthServerConfiguration  extends AuthorizationServerConfigurerAdapter{
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final ClientDetailsService clientDetailsService;

    // customized grant types
    private final SmsCodeTokenGranter smsCodeTokenGranter;
    private final WeChatTokenGranter weChatTokenGranter;
    private final WeChatWebTokenGranter weChatWebTokenGranter;

    private final AuthorizationServerTokenServices tokenService;
    private final TokenStore tokenStore;
    private final OAuth2RequestFactory requestFactory;

    public OAuthServerConfiguration(
            @Qualifier("oauth2AuthenticationManager") AuthenticationManager authenticationManager,
            @Qualifier("oauth2UserDetailsService") UserDetailsService userDetailsService,
            ClientDetailsService clientDetailsService,
            @Qualifier("oauth2TokenService") AuthorizationServerTokenServices tokenService,
            @Qualifier("oauth2RedisTokenStore") TokenStore tokenStore,
            OAuth2RequestFactory requestFactory,
            @Autowired(required = false) SmsCodeTokenGranter smsCodeTokenGranter,
            @Autowired(required = false) WeChatTokenGranter weChatTokenGranter,
            @Autowired(required = false) WeChatWebTokenGranter weChatWebTokenGranter) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.clientDetailsService = clientDetailsService;
        this.smsCodeTokenGranter = smsCodeTokenGranter;
        this.weChatTokenGranter = weChatTokenGranter;
        this.tokenService = tokenService;
        this.tokenStore = tokenStore;
        this.requestFactory = requestFactory;
        this.weChatWebTokenGranter = weChatWebTokenGranter;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security
                .checkTokenAccess("permitAll()")
                .allowFormAuthenticationForClients()
                .passwordEncoder(NoOpPasswordEncoder.getInstance())
        ;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetailsService);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        AccountAuthenticationConverter authenticationConverter = new AccountAuthenticationConverter();

        CustomizedAccessTokenConverter accessTokenConverter = new CustomizedAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(authenticationConverter);

        endpoints
                .userDetailsService(userDetailsService)
                .tokenStore(tokenStore)
                .tokenServices(tokenService)
                .tokenGranter(tokenGranter())
                .accessTokenConverter(accessTokenConverter)
        ;
    }

    protected TokenGranter tokenGranter() {
        // AuthorizationCodeServices authorizationCodeServices = authorizationCodeServices();

        List<TokenGranter> tokenGranters = new ArrayList<>();
        // tokenGranters.add(new AuthorizationCodeTokenGranter(tokenServices, authorizationCodeServices, clientDetailsService,  requestFactory));
        tokenGranters.add(new RefreshTokenGranter(tokenService, clientDetailsService, requestFactory));
        tokenGranters.add(new ImplicitTokenGranter(tokenService, clientDetailsService, requestFactory));
        tokenGranters.add(new ClientCredentialsTokenGranter(tokenService, clientDetailsService, requestFactory));
        tokenGranters.add(new ResourceOwnerPasswordTokenGranter(authenticationManager, tokenService, clientDetailsService, requestFactory));

        // sms_code
        if (smsCodeTokenGranter != null) {
            tokenGranters.add(smsCodeTokenGranter);
        }
        // we_chat
        if (weChatTokenGranter != null) {
            tokenGranters.add(weChatTokenGranter);
        }
        // we_chat_web
        if (weChatWebTokenGranter != null) {
            tokenGranters.add(weChatWebTokenGranter);
        }

        return new CompositeTokenGranter(tokenGranters);
    }

}
