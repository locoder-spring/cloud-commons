package icu.lowcoder.spring.cloud.authentication.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddWebPageWeChatBindingRequest {
    private String appId; // 公众号appId
    private String code; // 授权码
}
