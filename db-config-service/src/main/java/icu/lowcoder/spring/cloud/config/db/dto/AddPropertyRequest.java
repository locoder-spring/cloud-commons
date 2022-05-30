package icu.lowcoder.spring.cloud.config.db.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class AddPropertyRequest {
    @NotBlank
    private String application;
    @NotBlank
    private String profile;
    @NotBlank
    private String label;
    @NotBlank
    private String key;

    private String value;
}
