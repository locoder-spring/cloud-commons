package icu.lowcoder.spring.cloud.content.dto.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * @author  yanhan
 * description:
 * date:  create in 2021/3/1 2:07 下午
 */
@Data
public class BannerImageRequest {

    /**文件id*/
    @NotNull(message = "文件id不能为空")
    private UUID fileId;
    /**图片名称*/
    @NotEmpty(message = "图片名称不能为空")
    private String name;
    /**路径*/
    @NotEmpty(message = "路径不能为空")
    private String path;
}
