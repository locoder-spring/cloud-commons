package icu.lowcoder.spring.cloud.authentication.controller.management;

import icu.lowcoder.spring.commons.sms.PhoneNumberUtils;
import icu.lowcoder.spring.cloud.authentication.Constants;
import icu.lowcoder.spring.cloud.authentication.dao.AccountRepository;
import icu.lowcoder.spring.cloud.authentication.dto.*;
import icu.lowcoder.spring.cloud.authentication.entity.Account;
import icu.lowcoder.spring.cloud.authentication.service.management.ManagementAccountsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@RestController
public class ManagementAccountsController implements ManagementAccountsService {
    @Autowired
    private AccountRepository accountRepository;

    @Override
    public Page<ManagementAccountsResponse> page(
        @Valid KeywordParams params,
        @PageableDefault(sort = {"registerTime", "createdTime"}, direction = Sort.Direction.DESC) Pageable pageable) {

        return accountRepository.findAll((root, cq, cb) -> {
            if (StringUtils.hasText(params.getKeyword())) {
                String v = "%" + params.getKeyword().trim() + "%";
                return cb.or(
                        cb.like(root.get("phone"), v),
                        cb.like(root.get("name"), v),
                        cb.like(root.get("qq"), v),
                        cb.like(root.get("email"), v)
                );
            }
            return null;
        }, pageable).map(account -> {
            ManagementAccountsResponse response = new ManagementAccountsResponse();
            BeanUtils.copyProperties(account, response, "authorities");
            if (StringUtils.hasText(account.getAuthorities())) {
                response.setAuthorities(Arrays.asList(account.getAuthorities().split(",")));
            }
            return response;
        });
    }

    @Override
    @Transactional
    public void updateStatus(@PathVariable UUID accountId, @Valid @RequestBody UpdateAccountStatusRequest request) {
        Account account = queryAccount(accountId);

        account.setEnabled(request.getEnabled());
    }

    @Override
    @Transactional
    public void emptyPassword(@PathVariable UUID accountId) {
        Account account = queryAccount(accountId);

        account.setPassword(Constants.EMPTY_ENCODED_PASSWORD);
    }

    @Override
    @Transactional
    public void setAuthorities(@PathVariable UUID accountId, @Valid @RequestBody SetAccountAuthoritiesRequest request) {
        Account account = queryAccount(accountId);

        if (!request.getAuthorities().isEmpty()) {
            account.setAuthorities(StringUtils.collectionToCommaDelimitedString(request.getAuthorities()));
        } else {
            account.setAuthorities(null);
        }
    }

    @Override
    @Transactional
    public UUIDIdResponse add(@Valid @RequestBody AddAccountRequest request) {
        if (!PhoneNumberUtils.isPhoneNumber(request.getPhone())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "请使用11位数字手机号");
        }

        if (accountRepository.existsByPhone(request.getPhone())) {
            throw new HttpClientErrorException(HttpStatus.CONFLICT, "已存在该手机号");
        }

        Account account = new Account();
        BeanUtils.copyProperties(request, account, "authorities");

        if (!request.getAuthorities().isEmpty()) {
            account.setAuthorities(StringUtils.collectionToCommaDelimitedString(request.getAuthorities()));
        }

        account.setRegisterTime(new Date());
        accountRepository.save(account);

        return new UUIDIdResponse(account.getId());
    }

    @Override
    @Transactional
    public void del(@PathVariable UUID accountId) {
        Account account = queryAccount(accountId);

        accountRepository.delete(account);
    }

    private Account queryAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "账户不存在"));
    }
}
