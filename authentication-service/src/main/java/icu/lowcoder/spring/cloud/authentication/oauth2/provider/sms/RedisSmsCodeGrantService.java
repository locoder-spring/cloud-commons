package icu.lowcoder.spring.cloud.authentication.oauth2.provider.sms;

import icu.lowcoder.spring.cloud.authentication.util.RandomSmsCodeGenerator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.concurrent.TimeUnit;

public class RedisSmsCodeGrantService implements SmsCodeGrantService {
    private static final String DEF_KEY_PREFIX = "_LOWCODER_SPRING_CLOUD_AUTH_SMS_CODE#";
    private static final long DEF_EXPIRE_IN_SECOND = 5 * 60;

    private final RandomSmsCodeGenerator smsCodeGenerator;
    private final RedisTemplate<String, OAuth2Authentication> redisTemplate = new RedisTemplate<>();
    private final String keyPrefix;
    private final long expireInSecond;


    public RedisSmsCodeGrantService(RedisConnectionFactory redisConnectionFactory, RandomSmsCodeGenerator smsCodeGenerator) {
        this(redisConnectionFactory, smsCodeGenerator, DEF_KEY_PREFIX, DEF_EXPIRE_IN_SECOND);
    }

    public RedisSmsCodeGrantService(RedisConnectionFactory redisConnectionFactory, RandomSmsCodeGenerator smsCodeGenerator, String keyPrefix, long expireInSecond) {
        this.redisTemplate.setConnectionFactory(redisConnectionFactory);

        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        JdkSerializationRedisSerializer valueSerializer = new JdkSerializationRedisSerializer();
        this.redisTemplate.setKeySerializer(keySerializer);
        this.redisTemplate.setValueSerializer(valueSerializer);
        this.redisTemplate.setHashKeySerializer(keySerializer);
        this.redisTemplate.setHashValueSerializer(valueSerializer);

        this.redisTemplate.afterPropertiesSet();

        this.keyPrefix = keyPrefix;
        this.expireInSecond = expireInSecond;
        this.smsCodeGenerator = smsCodeGenerator;
    }

    @Override
    public String createSmsCode(OAuth2Authentication authentication) {
        String code = this.smsCodeGenerator.generate();
        this.store(code, authentication);
        return code;
    }

    @Override
    public OAuth2Authentication consumeSmsCode(String code) throws InvalidGrantException {
        OAuth2Authentication authentication = null;
        String key = generateKey(code);
        try {
            authentication = redisTemplate.opsForValue().get(key);
            redisTemplate.delete(key);
        } catch (Exception ignored) {
        }

        return authentication;
    }

    private void store(String code, OAuth2Authentication authentication) {
        redisTemplate.opsForValue().set(generateKey(code), authentication, expireInSecond, TimeUnit.SECONDS);
    }

    private String generateKey(String code) {
        return keyPrefix + "#" + code;
    }
}
