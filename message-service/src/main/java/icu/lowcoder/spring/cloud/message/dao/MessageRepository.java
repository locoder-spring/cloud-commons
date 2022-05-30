package icu.lowcoder.spring.cloud.message.dao;

import icu.lowcoder.spring.cloud.message.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Date;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID>, JpaSpecificationExecutor<Message> {
    Page<Message> findByInQueueFalseAndSentFalseAndSendTimeBefore(Date curr, Pageable pageRequest);
}
