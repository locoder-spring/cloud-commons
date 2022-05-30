package icu.lowcoder.spring.cloud.message.service;

import icu.lowcoder.spring.cloud.message.model.MessageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/messages")
public interface MessagesService {

    @PostMapping
    void add(MessageRequest request);

}
