package icu.lowcoder.spring.cloud.authentication.controller.management;

import icu.lowcoder.spring.cloud.authentication.dao.AccountRepository;
import icu.lowcoder.spring.cloud.authentication.dao.WeChatAppBindingRepository;
import icu.lowcoder.spring.cloud.authentication.dto.ManagementAccountWeChatBindingsResponse;
import icu.lowcoder.spring.cloud.authentication.dto.ManagementListAccountWeChatBindingsParams;
import icu.lowcoder.spring.cloud.authentication.entity.Account;
import icu.lowcoder.spring.cloud.authentication.entity.WeChatAppBinding;
import icu.lowcoder.spring.cloud.authentication.service.management.ManagementAccountWeChatBindingsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.UUID;

@RestController
public class ManagementAccountWeChatBindingsController implements ManagementAccountWeChatBindingsService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private WeChatAppBindingRepository bindingRepository;

    @Override
    public Page<ManagementAccountWeChatBindingsResponse> page(
            @PathVariable UUID accountId,
            @Valid ManagementListAccountWeChatBindingsParams params,
            @PageableDefault(sort = {"accountId", "lastModifiedTime", "createdTime"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Account account = queryAccount(accountId);

        return bindingRepository.findAll(
            Specification.<WeChatAppBinding>where((root, cq, cb) -> {
                if (StringUtils.hasText(params.getAppId())) {
                    String v = "%" + params.getAppId().trim() + "%";
                    return cb.like(root.get("appId"), v);
                }
                return null;
            })
            .and((root, cq, cb) -> cb.equal(root.get("account"), account))
            , pageable)
        .map(binding -> {
            ManagementAccountWeChatBindingsResponse response = new ManagementAccountWeChatBindingsResponse();
            BeanUtils.copyProperties(binding, response);
            return response;
        });
    }

    @Override
    @Transactional
    public void del(@PathVariable UUID accountId, @PathVariable UUID bindingId) {
        Account account = queryAccount(accountId);
        WeChatAppBinding binding = bindingRepository.findOneByAccountAndId(account, bindingId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "绑定信息不存在"));

        bindingRepository.delete(binding);
    }

    private Account queryAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "账户不存在"));
    }
}
