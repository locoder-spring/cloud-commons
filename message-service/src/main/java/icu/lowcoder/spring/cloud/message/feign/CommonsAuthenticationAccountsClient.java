package icu.lowcoder.spring.cloud.message.feign;

import icu.lowcoder.spring.commons.feign.ServiceAuthenticateFeignConfiguration;
import icu.lowcoder.spring.commons.feign.page.FeignPage;
import icu.lowcoder.spring.cloud.message.feign.model.Account;
import icu.lowcoder.spring.cloud.message.feign.model.WeChatBinding;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;
import java.util.UUID;

@FeignClient(contextId = "CommonsAuthenticationAccountsClient", name = "auth-service", configuration = ServiceAuthenticateFeignConfiguration.class)
public interface CommonsAuthenticationAccountsClient {

    @GetMapping("/accounts")
    FeignPage<Account> pageAccountsByIds(@RequestParam("ids") Set<UUID> ids, Pageable pageable);

    @GetMapping("/accounts/{accountId}/we-chat-bindings/{appId}?byAppId")
    WeChatBinding getWeChatBindingByAppId(
            @PathVariable UUID accountId,
            @PathVariable String appId);

    @GetMapping("/accounts/{accountId}")
    Account getById(@PathVariable UUID accountId);

}
