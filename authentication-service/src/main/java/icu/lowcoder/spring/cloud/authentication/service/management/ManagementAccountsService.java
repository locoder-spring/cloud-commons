package icu.lowcoder.spring.cloud.authentication.service.management;

import icu.lowcoder.spring.cloud.authentication.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/management/accounts")
public interface ManagementAccountsService {

    @GetMapping
    Page<ManagementAccountsResponse> page(KeywordParams params, Pageable pageable);

    @PostMapping(value = "/{accountId}", params = "op=updateStatus")
    void updateStatus(@PathVariable UUID accountId, UpdateAccountStatusRequest request);

    @PostMapping(value = "/{accountId}", params = "op=emptyPassword")
    void emptyPassword(@PathVariable UUID accountId);

    @PostMapping(value = "/{accountId}/authorities")
    void setAuthorities(@PathVariable UUID accountId, SetAccountAuthoritiesRequest request);

    @PostMapping
    UUIDIdResponse add(AddAccountRequest request);

    @DeleteMapping("/{accountId}")
    void del(@PathVariable UUID accountId);

}
