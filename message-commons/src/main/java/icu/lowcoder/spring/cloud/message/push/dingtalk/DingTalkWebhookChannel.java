package icu.lowcoder.spring.cloud.message.push.dingtalk;

import icu.lowcoder.spring.cloud.message.push.PushChannel;
import icu.lowcoder.spring.cloud.message.push.dingtalk.content.Content;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("ding_talk_webhook")
public class DingTalkWebhookChannel implements PushChannel {
    @JsonIgnore
    private String name = "ding_talk_webhook";

    @Override
    public String getChannel() {
        return name;
    }

    private Content content;
    private String webhook;
    private String secret;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DingTalkWebhookChannel channel;

        public Builder() {
            channel = new DingTalkWebhookChannel();
        }

        public Builder content(Content content) {
            this.channel.setContent(content);
            return this;
        }

        public Builder webhook(String webhook) {
            this.channel.setWebhook(webhook);
            return this;
        }
        public Builder secret(String secret) {
            this.channel.setSecret(secret);
            return this;
        }

        public DingTalkWebhookChannel build() {
            return channel;
        }
    }
}
