package icu.lowcoder.spring.cloud.content.dto.request;

import icu.lowcoder.spring.cloud.content.enums.JumpType;
import lombok.Data;

import java.util.Map;

/**
 * @author  yanhan
 * description:
 * date:  create in 2021/3/1 3:09 下午
 */
@Data
public class BannerJumpRequest {

    /**跳转方式*/
    private JumpType type;

    /**跳转参数*/
    private Map<String,Object> params;
}
