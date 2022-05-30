package icu.lowcoder.spring.cloud.content.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 客户类型
 */
@AllArgsConstructor
@Getter
public enum JumpType {
    URL("链接"),
    APP("APP"),
    WE_APP("微信小程序"),
    OTHER("OTHER")
    ;
    private final String description;
}
