package icu.lowcoder.spring.cloud.content.service.management;

import icu.lowcoder.spring.cloud.content.dto.request.BannerQueryRequest;
import icu.lowcoder.spring.cloud.content.dto.request.BannerRequest;
import icu.lowcoder.spring.cloud.content.dto.response.BannerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * @Author: yanhan
 * @Description:
 * @Date: create in 2021/3/1 1:49 下午
 */
@RequestMapping("/management/banners")
public interface BannerManagerService {

    @PostMapping
    UUID insertBanner(BannerRequest request);

    @PutMapping("/{bannerId}")
    UUID updateBanner(BannerRequest request, @PathVariable UUID bannerId);

    @GetMapping
    Page<BannerResponse> list(BannerQueryRequest request, Pageable pageable);

    @PatchMapping("/{bannerId}")
    UUID enabled(BannerRequest request, @PathVariable UUID bannerId);
}
