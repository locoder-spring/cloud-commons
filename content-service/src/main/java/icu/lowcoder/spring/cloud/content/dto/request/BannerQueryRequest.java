package icu.lowcoder.spring.cloud.content.dto.request;

import lombok.Data;

import java.util.Set;

/**
 * @Author: yanhan
 * @Description:
 * @Date: create in 2021/3/2 1:49 下午
 */
@Data
public class BannerQueryRequest {

    private String keyword;

    private Set<String> tags;


}
