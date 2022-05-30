package icu.lowcoder.spring.cloud.message.push.dingtalk.content;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class LinkContent implements Content {
    private String msgtype = "link";
    private Link link;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Link {
        private String text;
        private String title;
        private String picUrl;
        private String messageUrl;
    }

    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private Link link;

        public Builder() {
            link = new Link();
        }

        public Builder text(String text) {
            this.link.setText(text);
            return this;
        }
        public Builder title(String title) {
            this.link.setTitle(title);
            return this;
        }
        public Builder picUrl(String picUrl) {
            this.link.setPicUrl(picUrl);
            return this;
        }
        public Builder messageUrl(String messageUrl) {
            this.link.setMessageUrl(messageUrl);
            return this;
        }

        public LinkContent build() {
            LinkContent linkContent = new LinkContent();
            linkContent.setLink(this.link);

            return linkContent;
        }
    }
}
