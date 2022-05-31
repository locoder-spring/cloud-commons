package icu.lowcoder.spring.cloud.bridge.wechat.controller;

import icu.lowcoder.spring.cloud.bridge.wechat.config.WeChatBridgeProperties;
import icu.lowcoder.spring.cloud.bridge.wechat.dto.AuthorizeParams;
import icu.lowcoder.spring.cloud.bridge.wechat.manager.RedisAuthorizeCacheManager;
import icu.lowcoder.spring.cloud.bridge.wechat.model.AuthorizeCache;
import icu.lowcoder.spring.cloud.bridge.wechat.service.AuthorizeService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Slf4j
@Controller
public class AuthorizeController implements AuthorizeService {
    private static final String authorizeUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid={appId}&redirect_uri={redirectUrl}&response_type={responseType}&scope={scope}&state={state}#wechat_redirect";

    @Autowired
    private RedisAuthorizeCacheManager authorizeCacheManager;
    @Autowired
    private WeChatBridgeProperties bridgeProperties;

    @SneakyThrows
    @Override
    public void authorize(@Valid AuthorizeParams params, HttpServletRequest request, HttpServletResponse response) {
        String authorizeKey = RandomStringUtils.randomAlphanumeric(16);
        String locationUrl = buildAuthorizeUrl(
                params.getAppId(),
                bridgeProperties.getCallbackUrl() + "?key=" + authorizeKey,
                params.getScope(),
                params.getState());

        AuthorizeCache authorizeCache = new AuthorizeCache();
        authorizeCache.setRedirectUrl(params.getRedirectUrl());
        authorizeCache.setState(params.getState());
        authorizeCacheManager.cacheAuthorize(authorizeKey, authorizeCache);

        response.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
        response.setHeader("Location", locationUrl);
    }

    @Override
    public void callback(HttpServletRequest request, HttpServletResponse response) {
        if (log.isDebugEnabled()) {
            log.debug("##### authorize callback:");
            log.debug("params:");
            request.getParameterMap().forEach((name, value) ->
                    log.debug("\t\t" + name + ": " + StringUtils.arrayToDelimitedString(value, ",")));
            log.debug("#####");
        }

        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String key = request.getParameter("key");

        if (StringUtils.hasText(key)) {
            AuthorizeCache authorizeCache = authorizeCacheManager.getAuthorize(key);
            // matched
            if (authorizeCache != null && authorizeCache.getState().equals(state)) {
                // clear cache
                authorizeCacheManager.clearAuthorize(key);

                // redirect
                String location = authorizeCache.getRedirectUrl() +
                        (authorizeCache.getRedirectUrl().contains("?") ? "&" : "?") +
                        "code=" + code + "&" +
                        "state=" + state;
                response.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
                response.setHeader("Location", location);
                return;
            }
        }

        response.setStatus(HttpStatus.OK.value());
    }


    @SneakyThrows
    private static String buildAuthorizeUrl(String appId, String redirectUrl, String scope, String state) {
        return authorizeUrl.replaceAll("\\{appId\\}", appId)
                .replaceAll("\\{redirectUrl\\}", encodeUrl(redirectUrl))
                .replaceAll("\\{responseType\\}", "code")
                .replaceAll("\\{scope\\}", scope)
                .replaceAll("\\{state\\}", state);
    }

    private static String encodeUrl(String url) throws UnsupportedEncodingException {
        return URLEncoder.encode(url, "UTF-8")
                .replaceAll("\\+", "%20")
                .replaceAll("\\%21", "!")
                .replaceAll("\\%27", "'")
                .replaceAll("\\%28", "(")
                .replaceAll("\\%29", ")")
                .replaceAll("\\%7E", "~");
    }


}
