package icu.lowcoder.spring.cloud.message.push;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushResponse {
    private String response;
    private Boolean success;
    private String target;
}
