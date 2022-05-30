package icu.lowcoder.spring.cloud.message.model;

import icu.lowcoder.spring.commons.util.json.JsonUtils;
import icu.lowcoder.spring.cloud.message.push.PushChannel;
import icu.lowcoder.spring.cloud.message.push.email.EmailChannel;
import icu.lowcoder.spring.cloud.message.push.email.attachment.AliOssAttachment;
import icu.lowcoder.spring.cloud.message.push.sms.SmsChannel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class MessageRequest {
    private Set<String> tags = new HashSet<>();
    private Boolean broadcast = false;
    private Set<UUID> targets = new HashSet<>();
    private Boolean quiet = true;
    private Boolean visible = true;
    private String link;

    private String title;
    private String content;
    private Date submitTime;
    private Date sendTime;

    private List<PushChannel> pushChannels = new ArrayList<>();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MessageRequest request;

        public Builder() {
            request = new MessageRequest();
        }

        public Builder tags(List<String> tags) {
            this.request.setTags(new HashSet<>(tags));
            return this;
        }
        public Builder addTag(String tag) {
            this.request.getTags().add(tag);
            return this;
        }
        public Builder targets(List<UUID> targets) {
            this.request.setTargets(new HashSet<>(targets));
            return this;
        }
        public Builder addTarget(UUID target) {
            this.request.getTargets().add(target);
            return this;
        }
        public Builder broadcast(boolean broadcast) {
            this.request.setBroadcast(broadcast);
            return this;
        }
        public Builder broadcast() {
            this.request.setBroadcast(true);
            return this;
        }
        public Builder quiet(boolean quiet) {
            this.request.setQuiet(quiet);
            return this;
        }
        public Builder link(String link) {
            this.request.setLink(link);
            return this;
        }
        public Builder quiet() {
            this.request.setQuiet(true);
            return this;
        }
        public Builder visible(boolean visible) {
            this.request.setVisible(visible);
            return this;
        }
        public Builder visible() {
            this.request.setVisible(true);
            return this;
        }
        public Builder title(String title) {
            this.request.setTitle(title);
            return this;
        }
        public Builder content(String content) {
            this.request.setContent(content);
            return this;
        }
        public Builder sendTime(Date sendTime) {
            this.request.setSendTime(sendTime);
            return this;
        }
        public Builder submitTime(Date submitTime) {
            this.request.setSubmitTime(submitTime);
            return this;
        }
        public Builder pushChannel(List<PushChannel> channels) {
            this.request.setPushChannels(channels);
            return this;
        }
        public Builder addPushChannel(PushChannel channel) {
            this.request.getPushChannels().add(channel);
            return this;
        }

        public MessageRequest build() {
            return request;
        }
    }

    public static void main(String[] args) {
        MessageRequest request =
                MessageRequest.builder()
                        .addPushChannel(
                                EmailChannel.builder()
                                        .addAttachment(
                                                AliOssAttachment.builder()
                                                        .name("what's_this.gif")
                                                        .object("test/pangwa6.gif")
                                                        .build()
                                        )
                                        .build()
                        )
                        .addPushChannel(
                                SmsChannel.builder()
                                        .content("test")
                                        .build()
                        )
                        .build();

        String json = JsonUtils.toJson(request);
        System.out.println(json);

        MessageRequest dr = JsonUtils.parse(json, MessageRequest.class);

    }
}
