package icu.lowcoder.spring.cloud.authentication.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WeChatAppType {
    WEB_APP("公众号/网页", "we_chat_web"),
    MINI_PROGRAM("小程序", "we_chat"),
    ;

    private String description;
    private String grantType;
}
