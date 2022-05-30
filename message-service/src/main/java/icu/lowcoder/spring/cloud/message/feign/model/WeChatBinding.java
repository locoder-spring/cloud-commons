package icu.lowcoder.spring.cloud.message.feign.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeChatBinding {
    private String openId;
    private String appId;
    private String unionId;
    private String appType;
}
