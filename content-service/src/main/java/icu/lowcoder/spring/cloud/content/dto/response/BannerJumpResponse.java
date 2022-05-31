package icu.lowcoder.spring.cloud.content.dto.response;

import icu.lowcoder.spring.cloud.content.enums.JumpType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author  yanhan
 * description:
 * date:  create in 2021/3/2 1:42 下午
 */
@Data
@Builder
public class BannerJumpResponse {

    private JumpType type;

    /**跳转参数*/
    private Map<String,Object> params;
}
