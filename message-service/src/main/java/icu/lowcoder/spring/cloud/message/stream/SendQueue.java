package icu.lowcoder.spring.cloud.message.stream;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SendQueue implements ApplicationContextAware {
    @Autowired
    private Source source;

    private ApplicationContext applicationContext;

    public void add(UUID messageId) {
        source.output().send(MessageBuilder
                .withPayload(new SendQueuePayload(messageId))
                .setHeader("origin", applicationContext.getId())
                .build());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
