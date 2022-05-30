package icu.lowcoder.spring.cloud.message.task;

import icu.lowcoder.spring.cloud.message.dao.MessageRepository;
import icu.lowcoder.spring.cloud.message.entity.Message;
import icu.lowcoder.spring.cloud.message.manager.MessageSendManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class MessagePushTasks {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private MessageSendManager messageSendManager;

    @Async
    @Scheduled(cron="0 */2 * * * ?")
    public void sendMessage() {
        Date curr = new Date();

        Pageable pageRequest = PageRequest.of(0, 50, Sort.Direction.ASC, "submitTime");
        Page<Message> messages = messageRepository.findByInQueueFalseAndSentFalseAndSendTimeBefore(
                curr,
                pageRequest);

        while (!messages.getContent().isEmpty()) {
            messages.forEach(message -> {
                try {
                    messageSendManager.addToQueue(message.getId());
                } catch (Exception e) {
                    log.warn("推送消息异常", e);
                }
            });

            pageRequest = pageRequest.next();
            messages = messageRepository.findByInQueueFalseAndSentFalseAndSendTimeBefore(
                    curr,
                    pageRequest);
        }

    }

}
