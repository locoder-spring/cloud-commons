package icu.lowcoder.spring.cloud.authentication.service;

import icu.lowcoder.spring.commons.security.AccountModel;
import icu.lowcoder.spring.commons.wechat.model.UserInfo;
import icu.lowcoder.spring.commons.wechat.model.WebUserInfo;
import icu.lowcoder.spring.cloud.authentication.exception.UserRegistrationException;
import icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat.WeChatUserAccountModel;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface UserRegistrationService {
    AccountModel addUser(UserDetails user) throws UserRegistrationException;

    boolean alreadyExistPhone(String phone);

    WeChatUserAccountModel addWeChatUser(UserInfo weChatUser);

    void bindWeChatUser(UUID accountId, UserInfo weChatUser);

    WeChatUserAccountModel addWeChatWebUser(String appId, WebUserInfo webUserInfo);
}
