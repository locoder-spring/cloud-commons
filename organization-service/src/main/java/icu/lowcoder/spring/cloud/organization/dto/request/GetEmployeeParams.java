package icu.lowcoder.spring.cloud.organization.dto.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * 查询员工参数
 *
 * @author suchu
 * @date 2020/11/12
 */
@Data
public class GetEmployeeParams {
    @NotNull
    private UUID employeeId;
    @NotEmpty
    private List<String> positionCodes;
}
