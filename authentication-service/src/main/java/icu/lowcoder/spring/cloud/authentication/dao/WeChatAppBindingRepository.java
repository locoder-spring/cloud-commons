package icu.lowcoder.spring.cloud.authentication.dao;

import icu.lowcoder.spring.cloud.authentication.entity.Account;
import icu.lowcoder.spring.cloud.authentication.entity.WeChatAppBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WeChatAppBindingRepository extends JpaRepository<WeChatAppBinding, UUID>, JpaSpecificationExecutor<WeChatAppBinding> {
    WeChatAppBinding findOneByAccountAndAppId(Account account, String appId);

    List<WeChatAppBinding> findByAccountAndAppIdIn(Account account, List<String> appIds);

    Optional<WeChatAppBinding> findOneByAccountAndId(Account account, UUID bindingId);
}
