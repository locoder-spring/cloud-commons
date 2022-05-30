package icu.lowcoder.spring.cloud.message.push.wechat.web;

import icu.lowcoder.spring.cloud.message.push.PushChannel;
import icu.lowcoder.spring.cloud.message.push.wechat.MiniProgramState;
import icu.lowcoder.spring.cloud.message.push.wechat.WeChatSubscribeMessageDataValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 小程序订阅消息Channel
 *
 * @author suchu
 * @date 2021/4/19
 */
@Getter
@Setter
public class WeChatSubscribeChannel implements PushChannel {
    @JsonIgnore
    private String name = "we_chat_subscribe";

    private String appId;
    /**
     * 所需下发的订阅模板id
     */
    private String template;
    /**
     * 点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转。
     */
    private String page;
    /**
     * 模板内容，格式形如 { "key1": { "value": any }, "key2": { "value": any } }
     */
    private Map<String, WeChatSubscribeMessageDataValue> data = new HashMap<>();
    /**
     * 跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     */
    private MiniProgramState miniProgramState;
    /**
     * 进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
     */
    private String lang = "zh_CN";

    public static WeChatSubscribeChannel.Builder builder() {
        return new WeChatSubscribeChannel.Builder();
    }

    @Override
    public String getChannel() {
        return name;
    }

    public static class Builder {
        private WeChatSubscribeChannel channel;

        public Builder() {
            channel = new WeChatSubscribeChannel();
        }

        public WeChatSubscribeChannel.Builder template(String templateId) {
            this.channel.setTemplate(templateId);
            return this;
        }


        public WeChatSubscribeChannel.Builder miniProgramState(MiniProgramState miniProgramState) {
            this.channel.setMiniProgramState(miniProgramState);
            return this;
        }

        public WeChatSubscribeChannel.Builder lang(String lang) {
            this.channel.setLang(lang);
            return this;
        }

        public WeChatSubscribeChannel.Builder appId(String appId) {
            this.channel.setAppId(appId);
            return this;
        }

        public WeChatSubscribeChannel.Builder page(String url) {
            this.channel.setPage(url);
            return this;
        }


        public WeChatSubscribeChannel.Builder data(Map<String, WeChatSubscribeMessageDataValue> data) {
            this.channel.setData(data);
            return this;
        }

        public WeChatSubscribeChannel.Builder addData(String key, String value) {
            this.channel.getData().put(key, new WeChatSubscribeMessageDataValue(value));
            return this;
        }

        public WeChatSubscribeChannel build() {
            return channel;
        }
    }
}
