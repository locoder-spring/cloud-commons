package icu.lowcoder.spring.cloud.authentication.manager;

import icu.lowcoder.spring.commons.robot.RobotVerifier;
import icu.lowcoder.spring.commons.sms.PhoneNumberUtils;
import icu.lowcoder.spring.commons.sms.SmsSender;
import icu.lowcoder.spring.commons.sms.SmsType;
import icu.lowcoder.spring.cloud.authentication.dict.SmsCodeType;
import icu.lowcoder.spring.cloud.authentication.util.RandomSmsCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SmsCodeManager {
    private static final String SMS_CACHE_PREFIX = "icu.lowcoder.spring.cloud.auth.sms#";
    private static final String SEND_CACHE_PREFIX = "icu.lowcoder.spring.cloud.auth.sms.send#";

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RobotVerifier robotVerifier;
    @Autowired
    private SmsSender smsSender;

    private RandomSmsCodeGenerator smsCodeGenerator = new RandomSmsCodeGenerator();

    public void send(String phone, SmsCodeType type, Map<String, String> robotVerifyParams, HttpServletRequest httpServletRequest) {
        if (!robotVerifier.allow(httpServletRequest, robotVerifyParams)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "验证未通过");
        }

        if (StringUtils.isEmpty(phone)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "手机号不能为空");
        }
        if (!PhoneNumberUtils.isPhoneNumber(phone)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "请使用11位数字手机号");
        }

        // 一分钟内重复获取
        String sendCacheKey = buildSendCacheKey(type, phone);
        String sendCache = redisTemplate.opsForValue().get(sendCacheKey);
        if (org.springframework.util.StringUtils.hasText(sendCache)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "请勿频繁获取");
        }

        String code = smsCodeGenerator.generate();
        smsSender.send(phone, String.format("您的验证码为：%s，5分钟内有效，请勿告诉他人", code), SmsType.VERIFICATION_CODE);

        String smsCacheKey = buildSmsCacheKey(type, phone);
        redisTemplate.opsForValue().set(smsCacheKey, code, 300, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(sendCacheKey, code, 60, TimeUnit.SECONDS);
    }

    public String read(SmsCodeType type, String phone) {
        String smsCacheKey = buildSmsCacheKey(type, phone);
        String sms = redisTemplate.opsForValue().get(smsCacheKey);
        // 清除
        redisTemplate.delete(smsCacheKey);
        return sms;
    }

    private String buildSmsCacheKey(SmsCodeType type, String phone) {
        return SMS_CACHE_PREFIX + type + "#" + phone;
    }

    private String buildSendCacheKey(SmsCodeType type, String phone) {
        return SEND_CACHE_PREFIX + type + "#" + phone;
    }

}
