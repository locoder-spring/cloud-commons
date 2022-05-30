package icu.lowcoder.spring.cloud.organization.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Getter
@Setter
public class AddDepartmentRequest {
    private UUID parentId;

    @NotBlank
    private String name;
}
