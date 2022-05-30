package icu.lowcoder.spring.cloud.organization.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AddPositionRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String description;

    private List<UUID> authorities;
}
