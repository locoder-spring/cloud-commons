package icu.lowcoder.spring.cloud.bridge.wechat.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizeCache {
    private String redirectUrl;
    private String state;
}
