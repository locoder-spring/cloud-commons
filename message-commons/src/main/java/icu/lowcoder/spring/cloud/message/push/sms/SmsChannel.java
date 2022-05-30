package icu.lowcoder.spring.cloud.message.push.sms;

import icu.lowcoder.spring.cloud.message.push.PushChannel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("sms")
public class SmsChannel implements PushChannel {
    @JsonIgnore
    private String name = "sms";

    @Override
    public String getChannel() {
        return name;
    }

    private String content;
    private Boolean captcha = false;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SmsChannel channel;

        public Builder() {
            channel = new SmsChannel();
        }

        public Builder content(String content) {
            this.channel.setContent(content);
            return this;
        }

        public Builder captcha() {
            this.channel.captcha = true;
            return this;
        }
        public Builder captcha(boolean isCaptcha) {
            this.channel.captcha = isCaptcha;
            return this;
        }

        public SmsChannel build() {
            return channel;
        }
    }
}
