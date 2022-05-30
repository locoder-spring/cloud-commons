package icu.lowcoder.spring.cloud.message.controller;

import icu.lowcoder.spring.cloud.message.dao.MessageRepository;
import icu.lowcoder.spring.cloud.message.dao.MessageTagRepository;
import icu.lowcoder.spring.cloud.message.entity.Message;
import icu.lowcoder.spring.cloud.message.entity.MessageTag;
import icu.lowcoder.spring.cloud.message.feign.model.Account;
import icu.lowcoder.spring.cloud.message.manager.AccountsClientManager;
import icu.lowcoder.spring.cloud.message.model.MessageRequest;
import icu.lowcoder.spring.cloud.message.service.MessagesService;
import icu.lowcoder.spring.cloud.message.task.MessagePushTasks;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class MessagesController implements MessagesService {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private AccountsClientManager accountsClientManager;
    @Autowired
    private MessagePushTasks messagePushTasks;
    @Autowired
    private MessageTagRepository messageTagRepository;

    @Override
    @Transactional
    @PreAuthorize("#oauth2.client and #oauth2.clientHasRole('ROLE_SERVICE_CLIENT')")
    public void add(@Valid @RequestBody MessageRequest request) {
        if (request.getSubmitTime() == null) {
            request.setSubmitTime(new Date());
        }
        if (request.getSendTime() == null) {
            request.setSendTime(new Date());
        }

        if(request.getBroadcast()) {
            Message message = new Message();
            BeanUtils.copyProperties(request, message, "tags");
            message.getTags().addAll(processTags(request.getTags()));

            messageRepository.save(message);
        } else {
            List<Message> messages = new ArrayList<>();

            if (request.getTargets() != null && request.getTargets().size() > 50) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "单次消息发送目标不能超过50");
            }

            // 有目标
            if (request.getTargets() != null && !request.getTargets().isEmpty()) {
                List<Account> accounts = accountsClientManager.loadAccountsByIdIn(request.getTargets());
                if (!accounts.isEmpty()) {
                    messages.addAll(accounts.stream().map(account -> {
                        Message message = new Message();
                        BeanUtils.copyProperties(request, message, "tags");
                        message.getTags().addAll(processTags(request.getTags()));
                        message.setAccountId(account.getId());
                        return message;
                    }).collect(Collectors.toList()));
                }
            } else if (request.getPushChannels() != null && !request.getPushChannels().isEmpty()) { // 无目标，且通道都配置为空
                Message message = new Message();
                BeanUtils.copyProperties(request, message, "tags");
                message.getTags().addAll(processTags(request.getTags()));
                messages.add(message);
            }

            if (!messages.isEmpty()) {
                messageRepository.saveAll(messages);
            } else {
                Message message = new Message();
                BeanUtils.copyProperties(request, message, "tags");
                message.getTags().addAll(processTags(request.getTags()));
                message.setRemark("无法找到合适的消息发送对象");
                message.setSent(true);
                messageRepository.save(message);
            }
        }

        // 立即尝试推送
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                messagePushTasks.sendMessage();
            }
        });
    }

    private List<MessageTag> processTags(Set<String> tagNames) {
        tagNames = tagNames.stream().map(String::toLowerCase).collect(Collectors.toSet());
        List<MessageTag> tags = messageTagRepository.findAllByNameIn(tagNames);
        // new
        List<MessageTag> newTags = tagNames.stream()
                .filter(name -> tags.stream().noneMatch(t -> t.getName().equals(name)))
                .map(name -> {
                    MessageTag tag = new MessageTag();
                    tag.setName(name);
                    return tag;
                })
                .collect(Collectors.toList());
        if (!newTags.isEmpty()) {
            messageTagRepository.saveAll(newTags);
            tags.addAll(newTags);
        }

        return tags;
    }
}
