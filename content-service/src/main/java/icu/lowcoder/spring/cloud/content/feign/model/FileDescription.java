package icu.lowcoder.spring.cloud.content.feign.model;

import lombok.Data;

import java.util.UUID;

@Data
public class FileDescription {
    private UUID id;
    private String name;
    private Long size;
    private String path;

    private String type;
}
