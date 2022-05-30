package icu.lowcoder.spring.cloud.message.push;

import icu.lowcoder.spring.cloud.message.push.dingtalk.DingTalkWebhookChannel;
import icu.lowcoder.spring.cloud.message.push.email.EmailChannel;
import icu.lowcoder.spring.cloud.message.push.jpush.JPushChannel;
import icu.lowcoder.spring.cloud.message.push.sms.SmsChannel;
import icu.lowcoder.spring.cloud.message.push.wechat.web.WeChatSubscribeChannel;
import icu.lowcoder.spring.cloud.message.push.wechat.web.WeChatWebTplChannel;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "channel")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = SmsChannel.class, name = "sms"),
        @JsonSubTypes.Type(value = DingTalkWebhookChannel.class, name = "ding_talk_webhook"),
        @JsonSubTypes.Type(value = WeChatWebTplChannel.class, name = "we_chat_web_template"),
        @JsonSubTypes.Type(value = WeChatSubscribeChannel.class, name = "we_chat_subscribe"),
        @JsonSubTypes.Type(value = EmailChannel.class, name = "email"),
        @JsonSubTypes.Type(value = JPushChannel.class, name = "jpush"),
})
public interface PushChannel {
    String getChannel();
}
