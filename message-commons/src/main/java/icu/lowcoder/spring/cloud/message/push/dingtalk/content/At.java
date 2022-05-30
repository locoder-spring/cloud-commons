package icu.lowcoder.spring.cloud.message.push.dingtalk.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class At {
    private List<String> atMobiles;
    @JsonProperty(value = "isAtAll")
    private boolean atAll = false;

    public static At atAll() {
        At at = new At();
        at.setAtAll(true);
        return at;
    }
}

