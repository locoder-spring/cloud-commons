package icu.lowcoder.spring.cloud.authentication.controller;

import icu.lowcoder.spring.commons.security.SecurityUtils;
import icu.lowcoder.spring.commons.sms.PhoneNumberUtils;
import icu.lowcoder.spring.cloud.authentication.config.AuthProperties;
import icu.lowcoder.spring.cloud.authentication.dao.AccountRepository;
import icu.lowcoder.spring.cloud.authentication.dict.WeChatAppType;
import icu.lowcoder.spring.cloud.authentication.dto.*;
import icu.lowcoder.spring.cloud.authentication.entity.Account;
import icu.lowcoder.spring.cloud.authentication.service.AccountsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.expression.OAuth2ExpressionUtils;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class AccountsController implements AccountsService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthProperties authProperties;

    @Autowired
    @Qualifier("oauth2TokenService")
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @Autowired
    private ConsumerTokenServices consumerTokenServices;

    @Override
    public Object principal(OAuth2Authentication oAuth2Authentication) {
        return oAuth2Authentication.getPrincipal();
    }

    @Override
    public Object logout(OAuth2Authentication principal) {
        OAuth2AccessToken accessToken = authorizationServerTokenServices.getAccessToken(principal);
        if (accessToken != null && consumerTokenServices.revokeToken(accessToken.getValue())){
            return new ResponseEntity<>(HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public UUIDIdResponse register(@Valid @RequestBody RegisterRequest request) {
        if (accountRepository.existsByPhone(request.getPhone())) {
            throw new HttpClientErrorException(HttpStatus.PRECONDITION_FAILED, "该手机号已注册");
        }
        if (!request.getPhone().matches("\\d{11}")) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "手机号格式不正确：" + request.getPhone());
        }

        Account account = new Account();
        BeanUtils.copyProperties(request, account);

        account.setEnabled(true);
        account.setRegisterTime(new Date());

        if (StringUtils.hasText(request.getPassword())) {
            // TODO 弱口令处理
            if (request.getPassword().length() < authProperties.getMinPasswordLength()) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "密码长度至少为" + authProperties.getMinPasswordLength() + "位");
            }

            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        accountRepository.save(account);

        return new UUIDIdResponse(account.getId());
    }

    @Override
    @Transactional
    @PreAuthorize("(#oauth2.client and #oauth2.clientHasRole('ROLE_SERVICE_CLIENT'))")
    public List<AccountDetail> batchRegister(@Valid @RequestBody List<RegisterRequest> request) {
        if (request != null && !request.isEmpty()) {
            List<Account> exists = accountRepository.findByPhoneIn(request.stream().map(RegisterRequest::getPhone).collect(Collectors.toList()));
            // new accounts
            List<Account> newAccounts = request.stream()
                    .filter(r -> exists.stream().noneMatch(e -> e.getPhone().equals(r.getPhone())))
                    .map(r -> {
                        Account na = new Account();
                        BeanUtils.copyProperties(r, na);

                        na.setEnabled(true);
                        na.setRegisterTime(new Date());

                        return na;
                    })
                    .collect(Collectors.toList());

            if (!newAccounts.isEmpty()) {
                accountRepository.saveAll(newAccounts);

                return newAccounts.stream().map(a -> {
                    AccountDetail detail = new AccountDetail();
                    BeanUtils.copyProperties(a, detail);
                    return detail;
                }).collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    @Override
    @PreAuthorize("(#oauth2.client and #oauth2.clientHasRole('ROLE_SERVICE_CLIENT')) or #phone.equals(authentication.principal.phone)")
    public AccountDetail getByPhone(@RequestParam String phone) {
        return accountRepository.findOneByPhone(phone).map(a -> {
            AccountDetail detail = new AccountDetail();
            BeanUtils.copyProperties(a, detail);
            return detail;
        }).orElse(null);
    }

    @Override
    @PreAuthorize("(#oauth2.client and #oauth2.clientHasRole('ROLE_SERVICE_CLIENT'))")
    public List<AccountDetail> listByPhones(@RequestParam List<String> phones) {
        return accountRepository.findByPhoneIn(phones).stream().map(a -> {
            AccountDetail detail = new AccountDetail();
            BeanUtils.copyProperties(a, detail);
            return detail;
        }).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("(#oauth2.client and #oauth2.clientHasRole('ROLE_SERVICE_CLIENT')) or #id.equals(authentication.principal.id)")
    public AccountDetail get(@PathVariable UUID id) {
        return accountRepository.findById(id).map(a -> {
            AccountDetail detail = new AccountDetail();
            BeanUtils.copyProperties(a, detail);
            return detail;
        }).orElse(null);
    }

    @Override
    @Transactional
    @PreAuthorize("(#oauth2.client and #oauth2.clientHasRole('ROLE_SERVICE_CLIENT')) or #id.equals(authentication.principal.id)")
    public void update(@PathVariable UUID id, @Valid @RequestBody UpdateRequest request) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (!SecurityUtils.getPrincipalId().equals(id.toString())) {
            if (securityContext != null && securityContext.getAuthentication() != null) {
                OAuth2ExpressionUtils.isOAuthClientAuth(securityContext.getAuthentication());
            }
        }

        Optional<Account> accountOptional = accountRepository.findById(id);
        accountOptional.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "账户不存在"));

        Account account = accountOptional.get();
        if (StringUtils.hasText(request.getEmail())) {
            account.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getName())) {
            account.setName(request.getName());
        }
        if (StringUtils.hasText(request.getQq())) {
            account.setQq(request.getQq());
        }
        if (StringUtils.hasText(request.getPassword())) {
            // TODO 弱口令处理
            if (request.getPassword().length() < authProperties.getMinPasswordLength()) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "密码长度至少为" + authProperties.getMinPasswordLength() + "位");
            }

            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }
    }

    @Override
    @PreAuthorize("(#oauth2.client and #oauth2.clientHasRole('ROLE_SERVICE_CLIENT'))")
    public Page<AccountDetail> page(ListAccountsParams params, Pageable pageable) {
        Page<Account> accounts = accountRepository.findAll(Specification.where(((root, query, cb) -> {
            if (params.getIds() != null && !params.getIds().isEmpty()) {
                CriteriaBuilder.In<UUID> in = cb.in(root.get("id"));
                for (UUID id : params.getIds()) {
                    in.value(id);
                }
                return in;
            }
            return null;
        })), pageable);

        return accounts.map(a -> {
            AccountDetail detail = new AccountDetail();
            BeanUtils.copyProperties(a, detail);
            return detail;
        });
    }

    @Override
    @PreAuthorize("#accountId.equals(authentication.principal.id)")
    @Transactional
    public void replacePhone(@PathVariable UUID accountId, String phone) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "账号不存在"));
        if (!PhoneNumberUtils.isPhoneNumber(phone)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "请使用11位数字手机号");
        }

        // 检查认证
        // 目前的限制：需要绑定了微信并使用微信登录（小程序）的才可以更换手机号，且限制可以换绑手机的小程序
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2Authentication) {
            OAuth2Request oAuth2Request = ((OAuth2Authentication) authentication).getOAuth2Request();

            if (oAuth2Request.getGrantType().equals(WeChatAppType.MINI_PROGRAM.getGrantType()) &&   // 小程序登录
                    authProperties.getAllowReplacePhoneClients().contains(oAuth2Request.getClientId()) // 认证client允许更换手机
            ) {
                // 验证手机及验证码：
                if(accountRepository.existsByPhone(phone)) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "该手机号已注册了其他账号");
                }

                // 更换
                account.setPhone(phone);

                // 退出登录
                OAuth2AccessToken accessToken = authorizationServerTokenServices.getAccessToken((OAuth2Authentication) authentication);
                if (accessToken != null && consumerTokenServices.revokeToken(accessToken.getValue())){
                    consumerTokenServices.revokeToken(accessToken.getValue());
                }
                return;
            }
        }

        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "当前认证无法换绑手机");

     }

}
