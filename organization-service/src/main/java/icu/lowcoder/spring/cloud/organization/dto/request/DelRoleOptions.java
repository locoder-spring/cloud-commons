package icu.lowcoder.spring.cloud.organization.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DelRoleOptions {
    /**
     * 在删除后同步更新授权，默认不更新
     */
    private boolean updateAuthorizations = false;
}
