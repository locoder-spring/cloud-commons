package icu.lowcoder.spring.cloud.bridge.wechat.manager;

import icu.lowcoder.spring.cloud.bridge.wechat.config.WeChatBridgeProperties;
import icu.lowcoder.spring.cloud.bridge.wechat.model.AuthorizeCache;
import icu.lowcoder.spring.commons.util.json.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisAuthorizeCacheManager {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private WeChatBridgeProperties bridgeProperties;

    public AuthorizeCache getAuthorize(String key) {
        String cacheKey = buildKey(key);

        String json = null;
        Boolean hasKey = redisTemplate.hasKey(cacheKey);
        if (hasKey != null && hasKey) {
            json = redisTemplate.opsForValue().get(cacheKey);
        }

        if (json != null) {
            return JsonUtils.parse(json, AuthorizeCache.class);
        }

        return null;
    }

    public void cacheAuthorize(String key, AuthorizeCache authorize) {
        String cacheKey = buildKey(key);
        redisTemplate.opsForValue().set(cacheKey, JsonUtils.toJson(authorize), 60, TimeUnit.SECONDS);
    }

    public void clearAuthorize(String key) {
        String cacheKey = buildKey(key);
        redisTemplate.delete(cacheKey);
    }

    private String buildKey(String key) {
        return bridgeProperties.getAuthorizeCacheKeyPrefix() + "#" + key;
    }
}
