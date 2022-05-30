package icu.lowcoder.spring.cloud.message.push.wechat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeChatTemplateMessageDataValue {
    private String color = "#2db7f5";
    private String value;

    public WeChatTemplateMessageDataValue(String value) {
        this.value = value;
    }
}
