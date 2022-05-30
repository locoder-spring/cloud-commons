package icu.lowcoder.spring.cloud.message.dao;

import icu.lowcoder.spring.cloud.message.entity.MessageTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface MessageTagRepository extends JpaRepository<MessageTag, UUID>, JpaSpecificationExecutor<MessageTag> {
    List<MessageTag> findAllByNameIn(Set<String> tagNames);
}
