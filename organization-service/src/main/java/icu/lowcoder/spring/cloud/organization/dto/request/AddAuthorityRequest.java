package icu.lowcoder.spring.cloud.organization.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class AddAuthorityRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String description;
}
