package icu.lowcoder.spring.cloud.authentication.service;

import icu.lowcoder.spring.cloud.authentication.dto.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RequestMapping("/accounts/{accountId}/we-chat-bindings")
public interface AccountWeChatAppBindingsService {

    @PostMapping(params = "type=webApps")
    void addWebAppBinding(@PathVariable UUID accountId, AddWebPageWeChatBindingRequest request);

    @DeleteMapping(value = "/{appId}", params = "byAppId")
    void deleteAppBinding(@PathVariable UUID accountId, @PathVariable String appId);

    @PostMapping(params = "type=miniProgram")
    void addMiniProgramBinding(@PathVariable UUID accountId, AddWeAppBindingRequest request);

    @GetMapping(value = "/{appId}", params = "byAppId")
    WeChatBindingResponse getByAppId(@PathVariable UUID accountId, @PathVariable String appId);

    @PostMapping(params = "op=bindPhone")
    void bindPhone(@PathVariable UUID accountId, WeChatBindPhoneRequest request);

    @GetMapping(value = "/{appIds}", params = "byAppIds")
    List<WeChatBindingResponse> getByAppIds(@PathVariable UUID accountId, @PathVariable List<String> appIds);

}
