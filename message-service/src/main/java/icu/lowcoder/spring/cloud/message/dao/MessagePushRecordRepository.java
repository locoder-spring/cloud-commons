package icu.lowcoder.spring.cloud.message.dao;

import icu.lowcoder.spring.cloud.message.entity.MessagePushRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessagePushRecordRepository extends JpaRepository<MessagePushRecord, UUID> {
}
