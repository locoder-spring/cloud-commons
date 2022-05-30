package icu.lowcoder.spring.cloud.authentication.oauth2.provider.sms;

import icu.lowcoder.spring.commons.robot.RobotVerifier;
import icu.lowcoder.spring.commons.sms.SmsSender;
import icu.lowcoder.spring.commons.sms.SmsType;
import icu.lowcoder.spring.cloud.authentication.config.SmsGrantProperties;
import icu.lowcoder.spring.cloud.authentication.exception.InvalidSmsGrantException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@FrameworkEndpoint
public class SmsCodeAuthorizationEndpoint {

    private final SmsGrantProperties smsGrantProperties;

    private final SmsCodeGrantService smsCodeServices;

    private final RobotVerifier robotVerifier;

    private final OAuth2RequestFactory requestFactory;

    private final ClientDetailsService clientDetailsService;

    private final SmsSender smsSender;

    private final OAuth2RequestValidator oAuth2RequestValidator;

    public SmsCodeAuthorizationEndpoint(
            SmsGrantProperties smsGrantProperties,
            SmsCodeGrantService smsCodeServices,
            RobotVerifier robotVerifier,
            OAuth2RequestFactory requestFactory,
            ClientDetailsService clientDetailsService,
            SmsSender smsSender,
            OAuth2RequestValidator oAuth2RequestValidator) {
        this.smsGrantProperties = smsGrantProperties;
        this.smsCodeServices = smsCodeServices;
        this.robotVerifier = robotVerifier;
        this.requestFactory = requestFactory;
        this.clientDetailsService = clientDetailsService;
        this.smsSender = smsSender;
        this.oAuth2RequestValidator = oAuth2RequestValidator;
    }

    @RequestMapping(value = {"/oauth/sms-authorize"}, method = RequestMethod.POST)
    public ResponseEntity<String> authorize(@RequestParam Map<String, String> parameters, SessionStatus sessionStatus, HttpServletRequest request) {
        if (smsGrantProperties == null || smsGrantProperties.getEnabled() == null || !smsGrantProperties.getEnabled()) {
            throw new HttpServerErrorException(HttpStatus.FORBIDDEN, "短信授权已关闭或未配置");
        }

        AuthorizationRequest authorizationRequest = requestFactory.createAuthorizationRequest(parameters);

        if (!robotVerifier.allow(request, parameters)) {
            throw new InvalidSmsGrantException("获取短信验证码失败，请重试。");
        }

        if (authorizationRequest.getClientId() == null) {
            throw new InvalidClientException("A client id must be provided");
        } else {
            try {
                ClientDetails client = clientDetailsService.loadClientByClientId(authorizationRequest.getClientId());
                String phone = authorizationRequest.getRequestParameters().get("phone");
                if (!StringUtils.hasText(phone)) {
                    throw new InvalidRequestException("A phone number must be provided");
                } else {
                    // 手机号格式验证
                    if (!phone.matches("\\d{11}")) {
                        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "请使用11位数字手机号");
                    }

                    this.oAuth2RequestValidator.validateScope(authorizationRequest, client);

                    // 默认准许
                    authorizationRequest.setApproved(true);

                    // 生成并保存短信
                    Authentication authentication = new UsernamePasswordAuthenticationToken(phone, "N/A", client.getAuthorities());
                    String code = generateCode(authorizationRequest, authentication);

                    smsSender.send(phone, String.format("您的验证码为：%s，5分钟内有效，请勿告诉他人", code), SmsType.VERIFICATION_CODE);

                    return new ResponseEntity<>(HttpStatus.OK);
                }
            } catch (RuntimeException e) {
                sessionStatus.setComplete();
                throw e;
            }
        }
    }


    private String generateCode(AuthorizationRequest authorizationRequest, Authentication authentication) throws AuthenticationException {
        try {
            OAuth2Request storedOAuth2Request = requestFactory.createOAuth2Request(authorizationRequest);
            OAuth2Authentication combinedAuth = new OAuth2Authentication(storedOAuth2Request, authentication);
            return this.smsCodeServices.createSmsCode(combinedAuth);
        } catch (OAuth2Exception e) {
            if (authorizationRequest.getState() != null) {
                e.addAdditionalInformation("state", authorizationRequest.getState());
            }

            throw e;
        }
    }
}
