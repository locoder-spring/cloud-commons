package icu.lowcoder.spring.cloud.bridge.wechat.service;

import icu.lowcoder.spring.cloud.bridge.wechat.dto.JsApiSignRequest;
import icu.lowcoder.spring.cloud.bridge.wechat.dto.JsApiSignResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/jsapi")
public interface JavaScriptApiService {

    @PostMapping("/signature")
    JsApiSignResponse sign(JsApiSignRequest request);

}
