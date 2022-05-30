package icu.lowcoder.spring.cloud.authentication.dto;

import icu.lowcoder.spring.cloud.authentication.dict.WeChatAppType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class WeChatBindingResponse {
    private UUID id;
    private String openId;
    private String appId;
    private String unionId;
    private WeChatAppType appType;
}
