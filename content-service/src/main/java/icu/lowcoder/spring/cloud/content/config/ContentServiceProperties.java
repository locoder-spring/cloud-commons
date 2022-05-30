package icu.lowcoder.spring.cloud.content.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "icu.lowcoder.spring.cloud.content")
public class ContentServiceProperties {
    private List<String> publicReadContentTags = new ArrayList<>();
}
