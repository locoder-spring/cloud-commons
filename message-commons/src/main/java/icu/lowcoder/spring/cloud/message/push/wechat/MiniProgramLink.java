package icu.lowcoder.spring.cloud.message.push.wechat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MiniProgramLink {
    private String appId;
    private String pagePath;
}
