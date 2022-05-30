package icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat;

import icu.lowcoder.spring.commons.security.AccountModel;
import lombok.Setter;

@Setter
public class WeChatUserAccountModel extends AccountModel {

    private String openId;

    /**
     * username使用openId, 避免为空时在生成AccessToken等出现问题
     * @return openId
     */
    @Override
    public String getUsername() {
        return openId;
    }

}
