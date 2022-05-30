package icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat;

import icu.lowcoder.spring.commons.security.AccountModel;
import icu.lowcoder.spring.commons.util.json.JsonUtils;
import icu.lowcoder.spring.commons.wechat.model.UserInfo;
import icu.lowcoder.spring.cloud.authentication.config.AuthProperties;
import icu.lowcoder.spring.cloud.authentication.dict.WeChatAppType;
import icu.lowcoder.spring.cloud.authentication.entity.Account;
import icu.lowcoder.spring.cloud.authentication.exception.InvalidSmsGrantException;
import icu.lowcoder.spring.cloud.authentication.service.UserRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class WeChatTokenGranter extends AbstractTokenGranter {
    private final OAuth2RequestValidator oAuth2RequestValidator;
    private final WeChatGrantService weChatGrantService;
    private final UserRegistrationService userRegistrationService;
    private final AuthProperties authProperties;

    public WeChatTokenGranter(WeChatGrantService weChatGrantService, AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, OAuth2RequestValidator oAuth2RequestValidator, UserRegistrationService userRegistrationService, AuthProperties authProperties) {
        super(tokenServices, clientDetailsService, requestFactory, WeChatAppType.MINI_PROGRAM.getGrantType());
        this.weChatGrantService = weChatGrantService;
        this.oAuth2RequestValidator = oAuth2RequestValidator;
        this.userRegistrationService = userRegistrationService;
        this.authProperties = authProperties;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
        String code = parameters.get("code");
        String encryptedData = parameters.get("encrypted_data");
        String iv = parameters.get("iv");
        String appId = parameters.get("app_id");

        AuthorizationRequest authorizationRequest = this.getRequestFactory().createAuthorizationRequest(parameters);
        if (authorizationRequest.getClientId() == null) {
            throw new InvalidClientException("A client id must be provided");
        }
        oAuth2RequestValidator.validateScope(authorizationRequest, client);
        // 默认准许
        authorizationRequest.setApproved(true);

        if (code == null || iv == null || encryptedData == null) {
            throw new InvalidGrantException("认证信息不完整");
        } else {
            // 微信认证获取用户信息
            UserInfo weChatUser;
            try {
                weChatUser = weChatGrantService.decryptUserInfo(appId, code, encryptedData, iv);
            } catch (Exception e) {
                throw new InvalidGrantException("解密微信用户信息失败", e);
            }
            if (weChatUser == null) {
                throw new InvalidGrantException("未能解密微信用户信息");
            }

            WeChatUserAccountModel accountModel;
            // 用户查询
            Account account = weChatGrantService.loadAccountByWeChatUser(weChatUser);
            if (account == null) {
                if (authProperties.getAutoRegisterClients().contains(client.getClientId())) {
                    if (userRegistrationService == null) {
                        throw new AuthenticationServiceException("Auto register must provide UserRegistrationService instance");
                    }

                    accountModel = userRegistrationService.addWeChatUser(weChatUser);
                } else {
                    throw new InvalidGrantException("不存在微信用户");
                }
            } else {
                // 状态
                if (!account.getEnabled()) {
                    throw new InvalidSmsGrantException("账号已被禁用");
                }

                accountModel = new WeChatUserAccountModel();
                BeanUtils.copyProperties(account, accountModel);
                accountModel.setOpenId(weChatUser.getOpenId());

                // 尝试绑定openId
                userRegistrationService.bindWeChatUser(account.getId(), weChatUser);
            }

            Authentication authentication = new PreAuthenticatedAuthenticationToken(accountModel, weChatUser.getSessionKey(), client.getAuthorities());
            OAuth2Request auth2Request = getRequestFactory().createOAuth2Request(authorizationRequest);

            log.debug("WeChatTokenGrant, account id: {}, username:{}, phone:{}, weChatUser:{}",
                    accountModel.getId() == null ? "null" : accountModel.getId(),
                    accountModel.getUsername(),
                    accountModel.getPhone(),
                    JsonUtils.toJson(weChatUser)
            );
            return new OAuth2Authentication(auth2Request, authentication);
        }
    }
}
