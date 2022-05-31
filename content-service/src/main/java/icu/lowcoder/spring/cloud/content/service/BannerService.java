package icu.lowcoder.spring.cloud.content.service;

import icu.lowcoder.spring.cloud.content.dto.request.BannerQueryRequest;
import icu.lowcoder.spring.cloud.content.dto.response.BannerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author  yanhan
 * description:
 * date:  create in 2021/3/1 1:49 下午
 */
@RequestMapping("/banners")
public interface BannerService {



    @GetMapping
    Page<BannerResponse> list(BannerQueryRequest request, Pageable pageable);


}
