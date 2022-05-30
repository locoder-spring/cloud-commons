package icu.lowcoder.spring.cloud.message.push.dingtalk.content;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MarkdownContent implements Content {
    private String msgtype = "markdown";
    private Markdown markdown;
    private At at;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Markdown {
        private String title;
        private String text;
    }

    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private MarkdownContent content;
        private Markdown markdown;

        public Builder() {
            markdown = new Markdown();
            content = new MarkdownContent();
        }

        public Builder text(String text) {
            this.markdown.setText(text);
            return this;
        }

        public Builder title(String title) {
            this.markdown.setTitle(title);
            return this;
        }

        public Builder atMobiles(List<String> mobiles) {
            this.content.setAt(new At(mobiles, false));
            return this;
        }
        public Builder atAll() {
            this.content.setAt(At.atAll());
            return this;
        }

        public MarkdownContent build() {
            content.setMarkdown(markdown);
            return content;
        }
    }
}
