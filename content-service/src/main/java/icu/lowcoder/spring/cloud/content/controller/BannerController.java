package icu.lowcoder.spring.cloud.content.controller;

import icu.lowcoder.spring.cloud.content.dao.BannerRepository;
import icu.lowcoder.spring.cloud.content.dto.request.BannerQueryRequest;
import icu.lowcoder.spring.cloud.content.dto.response.BannerImageResponse;
import icu.lowcoder.spring.cloud.content.dto.response.BannerJumpResponse;
import icu.lowcoder.spring.cloud.content.dto.response.BannerResponse;
import icu.lowcoder.spring.cloud.content.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author  yanhan
 * description:
 * date:  create in 2021/3/1 1:48 下午
 */
@RestController
public class BannerController implements BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    @Override
    public Page<BannerResponse> list(BannerQueryRequest request, @PageableDefault Pageable pageable) {

        return bannerRepository.findAll((root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (!StringUtils.isEmpty(request.getKeyword())) {
                predicateList.add(cb.like(root.get("name"), "%" + request.getKeyword() + "%"));
            }
            predicateList.add(cb.equal(root.get("enabled"), true));

            Date date = new Date();
            //beginDate 小于等于
            predicateList.add(cb.lessThanOrEqualTo(root.get("beginDate"), date));
            //beginDate 大于等于
            predicateList.add(cb.greaterThanOrEqualTo(root.get("endDate"), date));
            if (request.getTags() != null && request.getTags().size() > 0) {
                predicateList.add(root.join("tags").get("name").in(request.getTags()));
            }
            query.orderBy(cb.asc(root.get("sequence")),
                    cb.desc(root.get("createdTime")));
            return cb.and(predicateList.toArray(new Predicate[0]));
        }, pageable).map(e -> {
            BannerImageResponse image = null;
            if (e.getImage() != null) {
                image = BannerImageResponse
                        .builder()
                        .id(e.getImage().getId())
                        .path(e.getImage().getPath())
                        .name(e.getImage().getName())
                        .build();
            }
            BannerJumpResponse jump = null;
            if (e.getJump() != null) {
                jump = BannerJumpResponse
                        .builder()
                        .type(e.getJump().getType())
                        .params(e.getJump().getParams())
                        .build();
            }

            return BannerResponse
                    .builder()
                    .id(e.getId())
                    .name(e.getName())
                    .enabled(e.isEnabled())
                    .periods(new Date[]{e.getBeginDate(), e.getEndDate()})
                    .sequence(e.getSequence())
                    .image(image)
                    .jump(jump)
                    .build();
        });
    }


}
