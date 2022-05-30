package icu.lowcoder.spring.cloud.message.support.hibernate.types;

import icu.lowcoder.spring.commons.util.json.JsonUtils;
import com.vladmihalcea.hibernate.type.util.JsonSerializer;

public class FixedJsonSerializer implements JsonSerializer {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T clone(T object) {
        return JsonUtils.parse(JsonUtils.toJson(object), (Class<T>)object.getClass());
    }

}
