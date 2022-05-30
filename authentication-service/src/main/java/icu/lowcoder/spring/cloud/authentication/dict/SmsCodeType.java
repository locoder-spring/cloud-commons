package icu.lowcoder.spring.cloud.authentication.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SmsCodeType {
    REPLACE_PHONE("换绑手机"),
    ;

    private String description;
}
