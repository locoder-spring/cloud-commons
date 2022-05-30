package icu.lowcoder.spring.cloud.authentication.service.management;

import icu.lowcoder.spring.cloud.authentication.dto.ManagementAccountWeChatBindingsResponse;
import icu.lowcoder.spring.cloud.authentication.dto.ManagementListAccountWeChatBindingsParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@RequestMapping("/management/accounts/{accountId}/we-chat-bindings")
public interface ManagementAccountWeChatBindingsService {

    @GetMapping
    Page<ManagementAccountWeChatBindingsResponse> page(@PathVariable UUID accountId,
                                                       ManagementListAccountWeChatBindingsParams params,
                                                       Pageable pageable);

    @DeleteMapping("/{bindingId}")
    void del(@PathVariable UUID accountId, @PathVariable UUID bindingId);


}
