package icu.lowcoder.spring.cloud.authentication.oauth2.provider.sms;

import icu.lowcoder.spring.cloud.authentication.Constants;
import icu.lowcoder.spring.cloud.authentication.config.AuthProperties;
import icu.lowcoder.spring.cloud.authentication.exception.InvalidSmsGrantException;
import icu.lowcoder.spring.cloud.authentication.service.UserRegistrationService;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.*;

public class SmsCodeTokenGranter extends AbstractTokenGranter {
    private final SmsCodeGrantService smsCodeServices;
    private final UserRegistrationService userRegistrationService;
    private final UserDetailsService userDetailsService;
    private final OAuth2RequestValidator oAuth2RequestValidator;
    private final AuthProperties authProperties;

    public SmsCodeTokenGranter(UserDetailsService userDetailsService, UserRegistrationService userRegistrationServiceService, SmsCodeGrantService smsCodeServices, AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, OAuth2RequestValidator oAuth2RequestValidator, AuthProperties authProperties) {
        this(userDetailsService, userRegistrationServiceService, smsCodeServices, tokenServices, clientDetailsService, requestFactory, oAuth2RequestValidator, "sms_code", authProperties);
    }

    protected SmsCodeTokenGranter(UserDetailsService userDetailsService, UserRegistrationService userRegistrationServiceService, SmsCodeGrantService smsCodeServices, AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, OAuth2RequestValidator oAuth2RequestValidator, String grantType, AuthProperties authProperties) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.smsCodeServices = smsCodeServices;
        this.userDetailsService = userDetailsService;
        this.oAuth2RequestValidator = oAuth2RequestValidator;
        this.userRegistrationService = userRegistrationServiceService;
        this.authProperties = authProperties;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
        String code = parameters.get("code");
        String phone = parameters.get("phone");

        this.oAuth2RequestValidator.validateScope(tokenRequest, client);

        if (code == null || phone == null) {
            throw new InvalidSmsGrantException("手机号和短信验证码不能为空");
        } else {
            OAuth2Authentication storedAuth = this.smsCodeServices.consumeSmsCode(code);
            if (storedAuth == null) {
                throw new InvalidSmsGrantException("短信验证码不正确");
            } else {
                OAuth2Request pendingOAuth2Request = storedAuth.getOAuth2Request();
                String pendingClientId = pendingOAuth2Request.getClientId();
                String clientId = tokenRequest.getClientId();

                if (!phone.equals(storedAuth.getPrincipal())) {
                    throw new InvalidSmsGrantException("短信验证码不匹配");
                }
                if (clientId != null && !clientId.equals(pendingClientId)) {
                    throw new InvalidSmsGrantException("未知的客户端");
                } else {
                    Map<String, String> combinedParameters = new HashMap<>(pendingOAuth2Request.getRequestParameters());
                    combinedParameters.putAll(parameters);
                    OAuth2Request finalStoredOAuth2Request = pendingOAuth2Request.createOAuth2Request(combinedParameters);
                    Authentication storedUserAuth = storedAuth.getUserAuthentication();
                    Authentication finalUserAuth;

                    if (authProperties.getAutoRegisterClients().contains(client.getClientId())) {
                        if (userRegistrationService == null) {
                            throw new AuthenticationServiceException("auto register must provide UserRegistrationService instance");
                        }

                        if(!userRegistrationService.alreadyExistPhone(phone)) {
                            UserDetails details = new User(phone, Constants.EMPTY_ENCODED_PASSWORD, storedUserAuth.getAuthorities());
                            userRegistrationService.addUser(details);
                        }
                    }

                    if(userRegistrationService.alreadyExistPhone(phone)) {
                        UserDetails details = userDetailsService.loadUserByUsername(phone);

                        List<GrantedAuthority> authorities = new ArrayList<>();
                        if (details.getAuthorities() != null) {
                            authorities.addAll(details.getAuthorities());
                        }

                        // 状态
                        if (!details.isEnabled()) {
                            throw new InvalidSmsGrantException("账号已被禁用");
                        }

                        finalUserAuth = new UsernamePasswordAuthenticationToken(details, "N/A", authorities);
                    } else {
                        logger.debug("user [" + phone + "] not existed.");
                        throw new InvalidSmsGrantException("用户名或密码错误");
                    }

                    return new OAuth2Authentication(finalStoredOAuth2Request, finalUserAuth);
                }
            }
        }
    }
}
