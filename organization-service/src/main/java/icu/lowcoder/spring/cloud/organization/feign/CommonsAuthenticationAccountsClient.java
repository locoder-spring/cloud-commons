package icu.lowcoder.spring.cloud.organization.feign;

import icu.lowcoder.spring.commons.feign.ServiceAuthenticateFeignConfiguration;
import icu.lowcoder.spring.cloud.organization.dto.response.UUIDIdResponse;
import icu.lowcoder.spring.cloud.organization.feign.model.AccountDetail;
import icu.lowcoder.spring.cloud.organization.feign.model.AccountRegisterRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "CommonsAuthenticationAccountsClient", name = "auth-service", configuration = ServiceAuthenticateFeignConfiguration.class)
public interface CommonsAuthenticationAccountsClient {

    @PostMapping("/accounts")
    UUIDIdResponse register(@RequestBody AccountRegisterRequest request);

    @GetMapping(value = "/accounts?byPhone")
    AccountDetail getByPhone(@RequestParam String phone);
}
