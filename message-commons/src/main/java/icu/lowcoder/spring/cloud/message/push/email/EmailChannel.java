package icu.lowcoder.spring.cloud.message.push.email;

import icu.lowcoder.spring.cloud.message.push.PushChannel;
import icu.lowcoder.spring.cloud.message.push.email.attachment.Attachment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonTypeName("email")
public class EmailChannel implements PushChannel {
    @JsonIgnore
    private String name = "email";

    @Override
    public String getChannel() {
        return name;
    }

    private String subject; // 主题
    private String fromName; // 来源名称（邮箱地址固定不能修改）
    private Set<String> cc = new HashSet<>(); // 抄送对象
    private Set<String> to = new HashSet<>(); // 目标
    private String html; // html内容
    private String plain;
    private List<Attachment> attachments = new ArrayList<>(); // 附件

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private EmailChannel channel;

        public Builder() {
            channel = new EmailChannel();
        }

        public Builder subject(String subject) {
            this.channel.setSubject(subject);
            return this;
        }
        public Builder fromName(String fromName) {
            this.channel.setFromName(fromName);
            return this;
        }
        public Builder html(String html) {
            this.channel.setHtml(html);
            return this;
        }
        public Builder plain(String plain) {
            this.channel.setPlain(plain);
            return this;
        }
        public Builder cc(Set<String> cc) {
            this.channel.setCc(cc);
            return this;
        }
        public Builder addCC(String cc) {
            this.channel.getCc().add(cc);
            return this;
        }
        public Builder to(Set<String> to) {
            this.channel.setTo(to);
            return this;
        }
        public Builder addTo(String to) {
            this.channel.getTo().add(to);
            return this;
        }
        public Builder attachments(List<Attachment> attachments) {
            this.channel.setAttachments(attachments);
            return this;
        }
        public Builder addAttachment(Attachment attachment) {
            this.channel.getAttachments().add(attachment);
            return this;
        }

        public EmailChannel build() {
            return channel;
        }
    }
}
