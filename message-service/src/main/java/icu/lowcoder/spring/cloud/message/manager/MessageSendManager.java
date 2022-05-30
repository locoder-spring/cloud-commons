package icu.lowcoder.spring.cloud.message.manager;

import icu.lowcoder.spring.cloud.message.MessageSendException;
import icu.lowcoder.spring.cloud.message.dao.MessageRepository;
import icu.lowcoder.spring.cloud.message.entity.Message;
import icu.lowcoder.spring.cloud.message.entity.MessagePushRecord;
import icu.lowcoder.spring.cloud.message.push.PushResponse;
import icu.lowcoder.spring.cloud.message.stream.SendQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class MessageSendManager {

    @Autowired
    private PushManager pushManager;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private SendQueue sendQueue;

    @Async
    @Transactional
    public void addToQueue(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageSendException("消息不存在"));

        if (!message.getSent() && message.getSendTime().after(new Date())) {
            throw new MessageSendException("该消息已经完成推送或还未到推送时间");
        }

        if (message.getInQueue()) {
            throw new MessageSendException("该消息已在推送队列中");
        }

        message.setInQueue(true);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                try {
                    sendQueue.add(messageId);
                } catch (Exception e) {
                    outOfQueue(messageId);
                }
            }
        });
    }

    @Transactional
    public void outOfQueue(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageSendException("消息不存在"));
        message.setInQueue(false);
    }

    @Transactional
    public void send(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageSendException("消息不存在"));

        if (!message.getSent() && message.getSendTime().after(new Date())) {
            throw new MessageSendException("该消息已经完成推送或还未到推送时间");
        }

        List<MessagePushRecord> records = new ArrayList<>();
        message.getPushChannels().forEach(channel -> {
            PushResponse response;
            try {
                response = pushManager.push(message, channel);
            } catch (Exception e) {
                response = new PushResponse();
                response.setSuccess(false);
                response.setResponse("未知推送错误:" + e.getMessage());
            }

            MessagePushRecord pushRecord = new MessagePushRecord();
            pushRecord.setChannel(channel.getChannel());
            pushRecord.setTarget(response.getTarget());
            pushRecord.setMessage(message);
            pushRecord.setSuccess(response.getSuccess());
            pushRecord.setResponse(response.getResponse());
            records.add(pushRecord);
        });

        message.setInQueue(false);
        message.setSent(true);
        message.getPushRecords().addAll(records);
    }
}
