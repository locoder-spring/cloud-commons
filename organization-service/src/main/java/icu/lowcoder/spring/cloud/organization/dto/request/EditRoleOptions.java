package icu.lowcoder.spring.cloud.organization.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class EditRoleOptions {
    /**
     * 在编辑后同步更新授权，默认不更新
     */
    private boolean updateAuthorizations = false;
}
