package icu.lowcoder.spring.cloud.content.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * @Author: yanhan
 * @Description:
 * @Date: create in 2021/3/2 1:39 下午
 */
@Data
@Builder
public class BannerImageResponse {

    /**id*/
    private UUID id;
    /**图片名称*/
    private String name;
    /**路径*/
    private String path;

    private UUID fileId;
}
