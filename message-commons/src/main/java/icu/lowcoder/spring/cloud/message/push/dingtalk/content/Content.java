package icu.lowcoder.spring.cloud.message.push.dingtalk.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "msgtype")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = ActionCardContent.class, name = "actionCard"),
        @JsonSubTypes.Type(value = LinkContent.class, name = "link"),
        @JsonSubTypes.Type(value = MarkdownContent.class, name = "markdown"),
        @JsonSubTypes.Type(value = TextContent.class, name = "text"),
})
public interface Content {
    String getMsgtype();
}
