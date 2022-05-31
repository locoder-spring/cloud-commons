package icu.lowcoder.spring.cloud.bridge.wechat.controller;

import icu.lowcoder.spring.cloud.bridge.wechat.config.WeChatBridgeProperties;
import icu.lowcoder.spring.cloud.bridge.wechat.dto.JsApiSignRequest;
import icu.lowcoder.spring.cloud.bridge.wechat.dto.JsApiSignResponse;
import icu.lowcoder.spring.cloud.bridge.wechat.service.JavaScriptApiService;
import icu.lowcoder.spring.commons.wechat.WeChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

import javax.validation.Valid;

@RestController
public class JavaScriptApiController implements JavaScriptApiService {

    @Autowired
    private WeChatClient weChatClient;
    @Autowired
    private WeChatBridgeProperties bridgeProperties;

    @Override
    public JsApiSignResponse sign(@Valid @RequestBody JsApiSignRequest request) {
        WeChatBridgeProperties.WeChatApp weChatApp = bridgeProperties.getApps().stream().filter(app -> app.getAppId().equals(request.getAppId())).findFirst().orElse(null);
        if (weChatApp == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "未配置微信[" + request.getAppId() + "]");
        }

        try {
            String signature = weChatClient.jsApiSign(weChatApp.getAppId(), weChatApp.getSecret(), request.getUrl(), request.getNonceStr(), request.getTimestamp());
            JsApiSignResponse response = new JsApiSignResponse();
            response.setSignature(signature);
            return response;
        } catch (Exception e) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "获取jsapi_ticket失败:" + e.getMessage());
        }
    }

}
