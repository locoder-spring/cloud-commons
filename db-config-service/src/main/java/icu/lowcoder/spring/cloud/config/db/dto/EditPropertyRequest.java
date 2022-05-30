package icu.lowcoder.spring.cloud.config.db.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class EditPropertyRequest {
    private String value;
}
