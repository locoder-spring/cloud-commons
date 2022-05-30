package icu.lowcoder.spring.cloud.message.stream;

import icu.lowcoder.spring.cloud.message.MessageSendException;
import icu.lowcoder.spring.cloud.message.manager.MessageSendManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SendQueueListeners {

    @Autowired
    private MessageSendManager messageSendManager;

    @StreamListener(value= Sink.INPUT)
    public void handle(SendQueuePayload payload) {
        log.info("Received SendQueue, message:{}", payload.toString());

        try {
            messageSendManager.send(payload.getMessageId());
        } catch (MessageSendException e) {
            log.warn("发送消息异常：{}", e.getMessage(), e);
        }
    }

}
