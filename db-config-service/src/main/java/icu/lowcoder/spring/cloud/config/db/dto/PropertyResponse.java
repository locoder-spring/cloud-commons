package icu.lowcoder.spring.cloud.config.db.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class PropertyResponse {
    private UUID id;
    private Date createdTime;
    private Date lastModifiedTime;
    private String application;
    private String profile;
    private String label;
    private String key;
    private String value;
}
