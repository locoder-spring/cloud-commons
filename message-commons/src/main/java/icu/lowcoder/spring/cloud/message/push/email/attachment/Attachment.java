package icu.lowcoder.spring.cloud.message.push.email.attachment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.InputStream;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = AliOssAttachment.class, name = "ali-oss"),
})
public interface Attachment {
    String getType();

    @JsonIgnore
    String getName();

    @JsonIgnore
    InputStream getInputStream();
}
