package icu.lowcoder.spring.cloud.message.push.email.attachment;

import com.aliyun.oss.model.OSSObject;
import icu.lowcoder.spring.commons.ali.oss.AliOssClient;
import icu.lowcoder.spring.commons.util.spring.SpringContextHolder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;

@Getter
@Setter
public class AliOssAttachment implements Attachment {

    @Override
    public String getType() {
        return "ali-oss";
    }

    @Override
    @JsonIgnore
    public String getName() {
        return originalName;
    }

    @Override
    @JsonIgnore
    public InputStream getInputStream() {
        AliOssClient aliOssClient = getAliOssClient();
        if (aliOssClient != null) {
            OSSObject ossObject = aliOssClient.getObject(object);
            return ossObject.getObjectContent();
        } else {
            return null;
        }
    }

    private String object;
    private String originalName;

    private AliOssClient getAliOssClient() {
        if (SpringContextHolder.applicationContext == null) {
            return null;
        }

        return SpringContextHolder.applicationContext.getBean(AliOssClient.class);
    }

    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private AliOssAttachment attachment;

        public Builder() {
            attachment = new AliOssAttachment();
        }

        public Builder object(String objectKey) {
            this.attachment.setObject(objectKey);
            return this;
        }
        public Builder name(String name) {
            this.attachment.setOriginalName(name);
            return this;
        }

        public AliOssAttachment build() {
            return attachment;
        }
    }


}
