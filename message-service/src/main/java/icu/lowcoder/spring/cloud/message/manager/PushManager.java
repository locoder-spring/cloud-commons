package icu.lowcoder.spring.cloud.message.manager;

import icu.lowcoder.spring.cloud.message.entity.Message;
import icu.lowcoder.spring.cloud.message.push.PushChannel;
import icu.lowcoder.spring.cloud.message.push.PushResponse;

public interface PushManager {
    PushResponse push(Message message, PushChannel channel);
}
