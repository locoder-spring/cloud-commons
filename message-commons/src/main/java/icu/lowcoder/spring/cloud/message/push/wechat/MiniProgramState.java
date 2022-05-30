package icu.lowcoder.spring.cloud.message.push.wechat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author suchu
 * @date 2021/4/19
 */
@Getter
@AllArgsConstructor
public enum MiniProgramState {
    DEVELOPER("开发板"),
    TRIAL("体验版"),
    FORMAL("正式版");

    private final String description;

    public String getVal() {
        return this.name().toLowerCase();
    }
}
