package icu.lowcoder.spring.cloud.message.push;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import icu.lowcoder.spring.commons.sms.SmsSender;
import icu.lowcoder.spring.commons.sms.SmsType;
import icu.lowcoder.spring.commons.util.json.JsonUtils;
import icu.lowcoder.spring.commons.util.spring.BeanUtils;
import icu.lowcoder.spring.commons.wechat.WeChatClient;
import icu.lowcoder.spring.commons.wechat.model.*;
import icu.lowcoder.spring.cloud.message.config.PushProperties;
import icu.lowcoder.spring.cloud.message.entity.Message;
import icu.lowcoder.spring.cloud.message.feign.CommonsAuthenticationAccountsClient;
import icu.lowcoder.spring.cloud.message.feign.model.Account;
import icu.lowcoder.spring.cloud.message.feign.model.WeChatBinding;
import icu.lowcoder.spring.cloud.message.manager.PushManager;
import icu.lowcoder.spring.cloud.message.push.dingtalk.DingTalkWebhookChannel;
import icu.lowcoder.spring.cloud.message.push.dingtalk.SignUtils;
import icu.lowcoder.spring.cloud.message.push.dingtalk.content.ActionCardContent;
import icu.lowcoder.spring.cloud.message.push.dingtalk.content.LinkContent;
import icu.lowcoder.spring.cloud.message.push.dingtalk.content.MarkdownContent;
import icu.lowcoder.spring.cloud.message.push.dingtalk.content.TextContent;
import icu.lowcoder.spring.cloud.message.push.email.EmailChannel;
import icu.lowcoder.spring.cloud.message.push.email.attachment.Attachment;
import icu.lowcoder.spring.cloud.message.push.sms.SmsChannel;
import icu.lowcoder.spring.cloud.message.push.wechat.web.WeChatSubscribeChannel;
import icu.lowcoder.spring.cloud.message.push.wechat.web.WeChatWebTplChannel;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class MultipleChannelPushManagerImpl implements PushManager {

    @Autowired
    private WeChatClient weChatClient;
    @Autowired
    private PushProperties pushProperties;
    @Autowired
    private CommonsAuthenticationAccountsClient accountsClient;
    @Autowired(required = false)
    private SmsSender smsSender;
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public PushResponse push(Message message, PushChannel channel) {
        if (channel instanceof DingTalkWebhookChannel) {
            return pushDingTalkWebhook(message, (DingTalkWebhookChannel) channel);
        }
        if (channel instanceof WeChatWebTplChannel) {
            return pushWeChatWebTemplate(message, (WeChatWebTplChannel) channel);
        }
        if (channel instanceof WeChatSubscribeChannel) {
            return pushWeChatSubscribe(message, (WeChatSubscribeChannel) channel);
        }
        if (channel instanceof SmsChannel) {
            return pushSms(message, (SmsChannel) channel);
        }
        if (channel instanceof EmailChannel) {
            return pushEmail(message, (EmailChannel) channel);
        }

        PushResponse resp = new PushResponse();
        resp.setResponse("未知通道");
        resp.setSuccess(false);
        return resp;
    }

    private PushResponse pushEmail(Message message, EmailChannel channel) {
        PushResponse resp = new PushResponse();
        if (mailSender == null) {
            resp.setSuccess(false);
            resp.setResponse("未正确配置MailSender");
            return resp;
        }

        try {
            if (!StringUtils.hasText(channel.getHtml()) && !StringUtils.hasText(channel.getPlain()) && !StringUtils.hasText(message.getContent())) {
                throw new RuntimeException("邮件内容不能为空");
            }
            if (!StringUtils.hasText(channel.getSubject()) && !StringUtils.hasText(message.getTitle())) {
                throw new RuntimeException("邮件主题不能为空");
            }

            if (channel.getTo().isEmpty() && message.getAccountId() == null) {
                throw new RuntimeException("消息未指定accountId");
            }

            if (channel.getTo().isEmpty()) {
                Account account = accountsClient.getById(message.getAccountId());
                if (account == null) {
                    throw new RuntimeException("未获取到账户账户信息");
                }

                if (account.getEmail() == null) {
                    throw new RuntimeException("账户未设置邮箱");
                }

                channel.getTo().add(account.getEmail());
            }

            // to and cc
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            if (!channel.getCc().isEmpty()) {
                helper.setCc(InternetAddress.parse(StringUtils.collectionToDelimitedString(channel.getCc(), ",")));
            }
            helper.setTo(InternetAddress.parse(StringUtils.collectionToDelimitedString(channel.getTo(), ",")));

            // from
            String fromName = channel.getFromName();
            if (!StringUtils.hasText(fromName)) {
                fromName = pushProperties.getEmail().getDefaultFromName();
            }
            helper.setFrom(new InternetAddress(pushProperties.getEmail().getFrom(), fromName));

            // subject
            String subject = channel.getSubject();
            if (subject == null) {
                subject = message.getTitle();
            }
            helper.setSubject(subject);

            // content
            String plain = channel.getPlain() != null ? channel.getPlain() : message.getContent();
            if (StringUtils.hasText(channel.getHtml())) {
                helper.setText(plain, channel.getHtml());
            } else {
                helper.setText(plain);
            }

            // attachments
            if (channel.getAttachments() != null && !channel.getAttachments().isEmpty()) {
                for (Attachment attachment : channel.getAttachments()) {
                    @Cleanup InputStream inputStream = attachment.getInputStream();
                    if (inputStream != null) {
                        File tmp = saveToTempFile(inputStream);
                        if (tmp != null) {
                            helper.addAttachment(attachment.getName(), tmp);
                        }
                    }
                }
            }

            mailSender.send(mimeMessage);
            resp.setTarget(StringUtils.collectionToDelimitedString(channel.getCc(), ","));
            resp.setSuccess(true);
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setResponse(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
        }

        return resp;
    }

    private File saveToTempFile(InputStream inputStream) {
        try {
            File file = File.createTempFile(UUID.randomUUID().toString(), null);
            @Cleanup OutputStream os = new FileOutputStream(file);
            IOUtils.copy(inputStream, os);
            return file;
        } catch (Exception e) {
            log.warn("Save temp file exception: {}", e.getMessage(), e);
        }
        return null;
    }

    private PushResponse pushSms(Message message, SmsChannel channel) {
        PushResponse resp = new PushResponse();
        if (smsSender == null) {
            resp.setSuccess(false);
            resp.setResponse("未正确配置SmsSender");
            return resp;
        }

        String content = channel.getContent();
        if (!StringUtils.hasText(content)) { // 如果无内容，使用 message.content
            content = message.getContent();
        }

        try {
            if (!StringUtils.hasText(content)) { // message.content 也没有内容则抛出异常
                throw new RuntimeException("短信内容不能为空");
            }

            if (message.getAccountId() == null) {
                throw new RuntimeException("消息未指定accountId");
            }

            Account account = accountsClient.getById(message.getAccountId());
            if (account == null) {
                throw new RuntimeException("未获取到账户账户信息");
            }

            String phone = account.getPhone();
            if (phone == null) {
                throw new RuntimeException("账户未设置手机号码");
            }

            smsSender.send(phone, content, channel.getCaptcha() ? SmsType.VERIFICATION_CODE : SmsType.OTHER);
            resp.setSuccess(true);
            resp.setTarget(phone);
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setResponse(e.getMessage());
        }

        return resp;
    }


    public PushResponse pushDingTalkWebhook(Message message, DingTalkWebhookChannel channel) {
        PushResponse resp = new PushResponse();

        try {
            if (!StringUtils.hasText(channel.getWebhook())) {
                throw new RuntimeException("webhook地址不能为空");
            }

            String webhook = channel.getWebhook();
            if (channel.getSecret() != null) {
                Long timestamp = System.currentTimeMillis();
                String sign = SignUtils.sign(timestamp, channel.getSecret());
                webhook = String.format("%s&%s=%s&%s=%s", webhook, "timestamp", timestamp, "sign", sign);
            }

            DingTalkClient dingTalkClient = new DefaultDingTalkClient(webhook);
            OapiRobotSendRequest request = new OapiRobotSendRequest();
            switch (channel.getContent().getMsgtype()) {
                case "actionCard":
                    ActionCardContent actionCardContent = (ActionCardContent) channel.getContent();
                    request.setActionCard(JsonUtils.toJson(actionCardContent.getActionCard()));
                    break;
                case "link":
                    LinkContent linkContent = (LinkContent) channel.getContent();
                    request.setLink(JsonUtils.toJson(linkContent.getLink()));
                    break;
                case "markdown":
                    MarkdownContent markdownContent = (MarkdownContent) channel.getContent();
                    // 处理at
                    if (markdownContent.getAt().getAtMobiles() != null
                            && !markdownContent.getAt().getAtMobiles().isEmpty()
                            && markdownContent.getMarkdown() != null
                    ) {
                        StringBuilder markdownText = new StringBuilder();
                        if (markdownContent.getMarkdown().getText() != null) {
                            markdownText.append(markdownContent.getMarkdown().getText());
                        }

                        for (String mobile : markdownContent.getAt().getAtMobiles()) {
                            markdownText.append(" @").append(mobile).append(" ");
                        }

                        markdownContent.getMarkdown().setText(markdownText.toString());
                    }
                    request.setMarkdown(JsonUtils.toJson(markdownContent.getMarkdown()));
                    request.setAt(JsonUtils.toJson(markdownContent.getAt()));
                    break;
                case "text":
                    TextContent textContent = (TextContent) channel.getContent();
                    // 处理at
                    if (textContent.getAt().getAtMobiles() != null
                            && !textContent.getAt().getAtMobiles().isEmpty()
                            && textContent.getText() != null
                    ) {
                        StringBuilder content = new StringBuilder();
                        if (textContent.getText().getContent() != null) {
                            content.append(textContent.getText().getContent());
                        }

                        for (String mobile : textContent.getAt().getAtMobiles()) {
                            content.append(" @").append(mobile).append(" ");
                        }

                        textContent.getText().setContent(content.toString());
                    }
                    request.setText(JsonUtils.toJson(textContent.getText()));
                    request.setAt(JsonUtils.toJson(textContent.getAt()));
                    break;
            }

            request.setMsgtype(channel.getContent().getMsgtype());

            OapiRobotSendResponse response = dingTalkClient.execute(request);
            resp.setResponse(response.getBody());
            resp.setTarget(webhook);
            if (response.isSuccess()) {
                resp.setSuccess(true);
            } else {
                resp.setSuccess(false);
            }
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setResponse(e.getMessage());
        }
        return resp;
    }

    public PushResponse pushWeChatWebTemplate(Message message, WeChatWebTplChannel channel) {
        PushResponse resp = new PushResponse();

        try {
            if (channel.getAppId() == null) {
                throw new RuntimeException("未指定appId");
            }

            PushProperties.WeChatApp weChatApp = pushProperties.getWeChat().getApps()
                    .stream()
                    .filter(app -> app.getAppId().equals(channel.getAppId()))
                    .findFirst()
                    .orElse(null);
            if (weChatApp == null) {
                throw new RuntimeException("未配置微信app：" + channel.getAppId());
            }

            // 微信绑定查询
            if (message.getAccountId() == null) {
                throw new RuntimeException("消息未指定accountId");
            }
            WeChatBinding binding = accountsClient.getWeChatBindingByAppId(message.getAccountId(), channel.getAppId());
            if (binding == null) {
                throw new RuntimeException("未获取到账户的微信绑定信息");
            }

            WebTemplateMessage webTemplateMessage = new WebTemplateMessage();
            webTemplateMessage.setToUser(binding.getOpenId());
            webTemplateMessage.setTemplate(channel.getTemplate());
            webTemplateMessage.setUrl(channel.getUrl());

            Map<String, TemplateMessageDataValue> dataMap = new HashMap<>();
            if (channel.getData() != null) {
                channel.getData().forEach((key, value) -> dataMap.put(key, BeanUtils.instantiate(TemplateMessageDataValue.class, value)));
            }
            webTemplateMessage.setData(dataMap);
            if (channel.getMiniProgramLink() != null) {
                MiniProgramLink miniProgramLink = new MiniProgramLink();
                miniProgramLink.setAppid(channel.getMiniProgramLink().getAppId());
                miniProgramLink.setPagepath(channel.getMiniProgramLink().getPagePath());
                webTemplateMessage.setMiniProgramLink(miniProgramLink);
            }

            WebTemplateMessageSendResponse response = weChatClient.sendWebTemplateMessage(weChatApp.getAppId(), weChatApp.getSecret(), webTemplateMessage);
            resp.setResponse(response.getMsgid());
            resp.setSuccess(true);
            resp.setTarget(binding.getOpenId());
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setResponse(e.getMessage());
        }

        return resp;
    }

    public PushResponse pushWeChatSubscribe(Message message, WeChatSubscribeChannel channel) {
        PushResponse resp = new PushResponse();
        try {
            if (channel.getAppId() == null) {
                throw new RuntimeException("未指定appId");
            }
            PushProperties.WeChatApp weChatApp = pushProperties.getWeChat().getApps()
                    .stream()
                    .filter(app -> app.getAppId().equals(channel.getAppId()))
                    .findFirst()
                    .orElse(null);
            if (weChatApp == null) {
                throw new RuntimeException("未配置微信app：" + channel.getAppId());
            }
            // 微信绑定查询
            if (message.getAccountId() == null) {
                throw new RuntimeException("消息未指定accountId");
            }
            WeChatBinding binding = accountsClient.getWeChatBindingByAppId(message.getAccountId(), channel.getAppId());
            if (binding == null) {
                throw new RuntimeException("未获取到账户的微信绑定信息");
            }

            SubscribeMessage subscribeMessage = new SubscribeMessage();
            subscribeMessage.setToUser(binding.getOpenId());
            subscribeMessage.setTemplate(channel.getTemplate());
            subscribeMessage.setPage(channel.getPage());
            Map<String, SubscribeMessageDataValue> dataMap = new HashMap<>();
            if (channel.getData() != null) {
                channel.getData().forEach((key, value) -> dataMap.put(key, BeanUtils.instantiate(SubscribeMessageDataValue.class, value)));
            }
            subscribeMessage.setData(dataMap);
            subscribeMessage.setMiniProgramState(channel.getMiniProgramState().getVal());
            subscribeMessage.setLang(channel.getLang());
            weChatClient.sendSubscribeMessage(weChatApp.getAppId(), weChatApp.getSecret(), subscribeMessage);
            resp.setResponse("");
            resp.setSuccess(true);
            resp.setTarget(binding.getOpenId());
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setResponse(e.getMessage());
        }

        return resp;
    }
}
