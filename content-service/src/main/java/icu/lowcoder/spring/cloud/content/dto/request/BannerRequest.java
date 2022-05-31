package icu.lowcoder.spring.cloud.content.dto.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * @author  yanhan
 * description:
 * date:  create in 2021/3/1 2:01 下午
 */
@Data
public class BannerRequest {

    private Set<String> tags = new HashSet<>();

    /**名称*/
    @NotEmpty(message = "名称不能为空")
    private String name;

    /**排序*/
    @NotNull(message = "排序不能为空")
    private Byte sequence;

    @NotNull(message = "图片信息不能为空")
    @Valid
    private BannerImageRequest image;

    private BannerJumpRequest jump;

    /**[时间起, 时间止]*/
    private Long[] periods;

    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;
}
