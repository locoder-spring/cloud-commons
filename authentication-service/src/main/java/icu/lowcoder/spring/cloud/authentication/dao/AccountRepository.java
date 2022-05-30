package icu.lowcoder.spring.cloud.authentication.dao;

import icu.lowcoder.spring.cloud.authentication.entity.Account;
import com.google.common.io.Files;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {
    Account findByPhone(String username);

    boolean existsByPhone(String username);

    Page<Account> findByIdIn(List<UUID> ids, Pageable pageable);

    Account findFirstByWeChatAppBindingsUnionId(String unionId);

    Optional<Account> findOneByPhone(String phone);

    Account findOneByWeChatAppBindingsOpenIdAndWeChatAppBindingsAppId(String openId, String appId);

    List<Account> findByPhoneIn(List<String> phones);
}
