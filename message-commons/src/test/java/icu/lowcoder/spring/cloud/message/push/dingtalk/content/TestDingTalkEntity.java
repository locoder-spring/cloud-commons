package icu.lowcoder.spring.cloud.message.push.dingtalk.content;

import icu.lowcoder.spring.commons.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author suchu
 * {@code @date} 2020/12/17
 */
@Slf4j
public class TestDingTalkEntity {
    @Test
    public void testAtAll() {
        At at = At.atAll();
        String jsonStr = JsonUtils.toJson(at);
        log.info(JsonUtils.toJson(at));
        Assert.assertTrue(jsonStr.contains("isAtAll"));

    }
}
