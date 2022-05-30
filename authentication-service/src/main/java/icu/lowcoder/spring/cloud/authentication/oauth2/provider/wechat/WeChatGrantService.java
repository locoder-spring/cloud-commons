package icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat;


import icu.lowcoder.spring.commons.wechat.model.UserInfo;
import icu.lowcoder.spring.commons.wechat.model.WebUserInfo;
import icu.lowcoder.spring.cloud.authentication.entity.Account;

public interface WeChatGrantService {
    UserInfo decryptUserInfo(String appId, String code, String encryptedData, String iv);

    WebUserInfo getWebUserInfo(String appId, String code);

    Account loadAccountByWeChatUser(UserInfo weChatUser);

    Account loadAccountByWeChatWebUser(String appId, WebUserInfo webUser);
}
