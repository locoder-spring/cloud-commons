package icu.lowcoder.spring.cloud.message.manager;

import icu.lowcoder.spring.commons.feign.page.FeignPage;
import icu.lowcoder.spring.cloud.message.feign.CommonsAuthenticationAccountsClient;
import icu.lowcoder.spring.cloud.message.feign.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AccountsClientManager {
    @Autowired
    private CommonsAuthenticationAccountsClient accountsClient;

    public List<Account> loadAccountsByIdIn(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        Pageable pageRequest = PageRequest.of(0, 100);
        FeignPage<Account> page;
        List<Account> accounts = new ArrayList<>();
        do {
            page = accountsClient.pageAccountsByIds(ids, pageRequest);
            accounts.addAll(page.getContent());

            pageRequest = pageRequest.next();
        } while (page.hasNext());

        return accounts;
    }

    public Account loadAccount(UUID id) {
        if (id == null) {
            return null;
        }

        List<Account> accountInfos = this.loadAccountsByIdIn(Collections.singleton(id));
        return accountInfos.isEmpty() ? null : accountInfos.get(0);
    }
}
