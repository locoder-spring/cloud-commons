package icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat;

import icu.lowcoder.spring.commons.wechat.WeChatClient;
import icu.lowcoder.spring.commons.wechat.model.SessionKey;
import icu.lowcoder.spring.commons.wechat.model.UserInfo;
import icu.lowcoder.spring.commons.wechat.model.WebUserAccessToken;
import icu.lowcoder.spring.commons.wechat.model.WebUserInfo;
import icu.lowcoder.spring.cloud.authentication.config.WeChatGrantProperties;
import icu.lowcoder.spring.cloud.authentication.dao.AccountRepository;
import icu.lowcoder.spring.cloud.authentication.entity.Account;
import org.apache.commons.lang.StringUtils;

public class WeChatGrantServiceImpl implements WeChatGrantService {
    private final AccountRepository accountRepository;
    private final WeChatClient weChatClient;
    private final WeChatGrantProperties weChatGrantProperties;

    public WeChatGrantServiceImpl(AccountRepository accountRepository, WeChatClient weChatClient, WeChatGrantProperties weChatGrantProperties) {
        this.accountRepository = accountRepository;
        this.weChatClient = weChatClient;
        this.weChatGrantProperties = weChatGrantProperties;
    }

    @Override
    public UserInfo decryptUserInfo(String appId, String code, String encryptedData, String iv) {
        WeChatApp weChatApp = weChatGrantProperties.getApps().stream().filter(app -> app.getAppId().equals(appId)).findFirst().orElse(null);
        if (weChatApp == null) {
            throw new RuntimeException("未配置微信认证[" + appId + "]");
        }

        SessionKey sessionKey = weChatClient.code2Session(code, weChatApp.getAppId(), weChatApp.getSecret());
        if (StringUtils.isNotBlank(sessionKey.getSessionKey())) {
            UserInfo userInfo = weChatClient.decryptData(encryptedData, sessionKey.getSessionKey(), iv, UserInfo.class);
            if (userInfo != null) {
                userInfo.setAppId(weChatApp.getAppId());
                userInfo.setSessionKey(sessionKey.getSessionKey());
                userInfo.setOpenId(sessionKey.getOpenid());
                userInfo.setUnionId(sessionKey.getUnionid());
                return userInfo;
            } else {
                throw new RuntimeException("解析微信用户json信息失败");
            }
        } else {
            throw new RuntimeException("获取小程序sessionKey失败:" + sessionKey.getErrmsg());
        }
    }

    @Override
    public WebUserInfo getWebUserInfo(String appId, String code) {
        WeChatApp weChatApp = weChatGrantProperties.getApps().stream().filter(app -> app.getAppId().equals(appId)).findFirst().orElse(null);
        if (weChatApp == null) {
            throw new RuntimeException("未配置微信认证[" + appId + "]");
        }

        WebUserAccessToken webUserAccessToken = weChatClient.getWebUserAccessToken(code, appId, weChatApp.getSecret());
        if (StringUtils.isNotBlank(webUserAccessToken.getAccessToken())) {
            WebUserInfo webUserInfo = new WebUserInfo();
            webUserInfo.setOpenid(webUserAccessToken.getOpenid());
            // 包含用户信息则获取详细信息
            if(webUserAccessToken.getScope().contains("snsapi_userinfo")) {
                webUserInfo = weChatClient.getWebUserInfo(webUserAccessToken.getAccessToken(), webUserAccessToken.getOpenid());
            }

            return webUserInfo;
        } else {
            throw new RuntimeException("获取公众号用户token失败:" + webUserAccessToken.getErrmsg());
        }
    }

    @Override
    public Account loadAccountByWeChatUser(UserInfo weChatUser) {
        // unionId
        Account account = null;
        if (StringUtils.isNotBlank(weChatUser.getUnionId())) {
            account = accountRepository.findFirstByWeChatAppBindingsUnionId(weChatUser.getUnionId());
        }

        // appId + openId
        if (account == null && StringUtils.isNotBlank(weChatUser.getAppId()) && StringUtils.isNotBlank(weChatUser.getOpenId())) {
            account = accountRepository.findOneByWeChatAppBindingsOpenIdAndWeChatAppBindingsAppId(weChatUser.getOpenId(), weChatUser.getAppId());
        }

        return account;
    }

    @Override
    public Account loadAccountByWeChatWebUser(String appId, WebUserInfo webUser) {
        // unionId
        Account account = null;
        if (StringUtils.isNotBlank(webUser.getUnionid())) {
            account = accountRepository.findFirstByWeChatAppBindingsUnionId(webUser.getUnionid());
        }

        // appId + openId
        if (account == null && StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(webUser.getOpenid())) {
            account = accountRepository.findOneByWeChatAppBindingsOpenIdAndWeChatAppBindingsAppId(webUser.getOpenid(), appId);
        }

        return account;
    }

}
