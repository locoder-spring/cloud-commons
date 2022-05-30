package icu.lowcoder.spring.cloud.authentication.dto;

import icu.lowcoder.spring.cloud.authentication.dict.WeChatAppType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class ManagementAccountWeChatBindingsResponse {
    private UUID id;
    private Date createdTime;
    private Date lastModifiedTime;
    private String appId;
    private WeChatAppType appType;
    private String openId;
    private String unionId;
}
