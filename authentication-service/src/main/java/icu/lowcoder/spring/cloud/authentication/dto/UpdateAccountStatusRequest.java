package icu.lowcoder.spring.cloud.authentication.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UpdateAccountStatusRequest {
    @NotNull
    private Boolean enabled;
}
