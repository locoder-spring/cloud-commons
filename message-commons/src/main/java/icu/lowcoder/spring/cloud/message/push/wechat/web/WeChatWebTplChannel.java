package icu.lowcoder.spring.cloud.message.push.wechat.web;

import icu.lowcoder.spring.cloud.message.push.PushChannel;
import icu.lowcoder.spring.cloud.message.push.wechat.MiniProgramLink;
import icu.lowcoder.spring.cloud.message.push.wechat.WeChatTemplateMessageDataValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class WeChatWebTplChannel implements PushChannel {
    @JsonIgnore
    private String name = "we_chat_web_template";

    @Override
    public String getChannel() {
        return name;
    }

    private String template;
    private String appId;
    private String url;
    private MiniProgramLink miniProgramLink;
    private Map<String, WeChatTemplateMessageDataValue> data = new HashMap<>();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private WeChatWebTplChannel channel;
        public Builder() {
            channel = new WeChatWebTplChannel();
        }

        public Builder template(String templateId) {
            this.channel.setTemplate(templateId);
            return this;
        }

        public Builder appId(String appId) {
            this.channel.setAppId(appId);
            return this;
        }

        public Builder url(String url) {
            this.channel.setUrl(url);
            return this;
        }

        public Builder miniProgram(String appId, String path) {
            this.channel.setMiniProgramLink(new MiniProgramLink(appId, path));
            return this;
        }

        public Builder data(Map<String, WeChatTemplateMessageDataValue> data) {
            this.channel.setData(data);
            return this;
        }
        public Builder addData(String key, String value) {
            this.channel.getData().put(key, new WeChatTemplateMessageDataValue(value));
            return this;
        }
        public Builder addData(String key, String value, String valueColor) {
            this.channel.getData().put(key, new WeChatTemplateMessageDataValue(value, valueColor));
            return this;
        }

        public WeChatWebTplChannel build() {
            return channel;
        }
    }
}
