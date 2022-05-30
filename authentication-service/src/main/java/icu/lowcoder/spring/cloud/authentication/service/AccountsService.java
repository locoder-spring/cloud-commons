package icu.lowcoder.spring.cloud.authentication.service;

import icu.lowcoder.spring.cloud.authentication.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/accounts")
public interface AccountsService {

    @GetMapping(params = {"principal"})
    Object principal(OAuth2Authentication principal);

    @PostMapping("/logout")
    Object logout(OAuth2Authentication principal);

    @PostMapping
    UUIDIdResponse register(RegisterRequest request);

    /**
     * ! only service client call
     * @param request info list
     * @return accounts
     */
    @PostMapping(params = "batch")
    List<AccountDetail> batchRegister(List<RegisterRequest> request);

    @GetMapping(params = "byPhone")
    AccountDetail getByPhone(String phone);

    /**
     * ! only service client call
     * @param phones phone numbers
     * @return accounts
     */
    @GetMapping(params = "byPhones")
    List<AccountDetail> listByPhones(List<String> phones);

    @GetMapping("/{id}")
    AccountDetail get(@PathVariable UUID id);

    @PutMapping("/{id}")
    void update(@PathVariable UUID id, UpdateRequest request);

    @GetMapping
    Page<AccountDetail> page(ListAccountsParams params, Pageable pageable);

    @PostMapping(value = "/{accountId}", params = "op=replacePhone")
    void replacePhone(@PathVariable UUID accountId, String phone);
}
