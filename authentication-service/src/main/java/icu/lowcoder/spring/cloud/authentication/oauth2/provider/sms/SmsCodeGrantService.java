package icu.lowcoder.spring.cloud.authentication.oauth2.provider.sms;

import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public interface SmsCodeGrantService {
    String createSmsCode(OAuth2Authentication authentication);

    OAuth2Authentication consumeSmsCode(String code) throws InvalidGrantException;
}
