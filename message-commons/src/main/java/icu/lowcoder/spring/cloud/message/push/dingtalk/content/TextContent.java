package icu.lowcoder.spring.cloud.message.push.dingtalk.content;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TextContent implements Content {
    private String msgtype = "text";
    private Text text;
    private At at;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Text {
        private String content;
    }

    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private TextContent textContent;

        public Builder() {
            textContent = new TextContent();
        }

        public Builder content(String content) {
            this.textContent.setText(new Text(content));
            return this;
        }

        public Builder atAll() {
            this.textContent.setAt(At.atAll());
            return this;
        }

        public Builder atMobiles(List<String> mobiles) {
            this.textContent.setAt(new At(mobiles, false));
            return this;
        }

        public TextContent build() {
            return textContent;
        }
    }
}
