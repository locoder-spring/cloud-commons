package icu.lowcoder.spring.cloud.message.push.jpush;

import icu.lowcoder.spring.cloud.message.push.PushChannel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

// TODO 实现极光的相关推送
@Getter
@Setter
@JsonTypeName("jpush")
public class JPushChannel implements PushChannel {
    @JsonIgnore
    private String name = "jpush";

    @Override
    public String getChannel() {
        return name;
    }

    private String content;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private JPushChannel channel;

        public Builder() {
            channel = new JPushChannel();
        }

        public Builder content(String content) {
            this.channel.setContent(content);
            return this;
        }

        public JPushChannel build() {
            return channel;
        }
    }
}
