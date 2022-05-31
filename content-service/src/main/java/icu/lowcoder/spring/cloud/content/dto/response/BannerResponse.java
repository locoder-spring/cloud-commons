package icu.lowcoder.spring.cloud.content.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 * @author  yanhan
 * description:
 * date:  create in 2021/3/2 1:38 下午
 */
@Data
@Builder
public class BannerResponse {

    private UUID id;

    /**名称*/
    private String name;

    /**排序*/
    private byte sequence;

    private BannerImageResponse image;

    private BannerJumpResponse jump;

    private Date[] periods;

    private Boolean enabled;
}
