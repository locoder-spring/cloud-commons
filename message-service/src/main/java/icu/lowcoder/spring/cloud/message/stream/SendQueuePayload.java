package icu.lowcoder.spring.cloud.message.stream;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@Data
public class SendQueuePayload implements Serializable {
    private UUID messageId;
    private Date queueTime = new Date();

    public SendQueuePayload(UUID messageId) {
        this.messageId = messageId;
    }
}
