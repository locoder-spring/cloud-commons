package icu.lowcoder.spring.cloud.organization.manager;

import icu.lowcoder.spring.commons.jpa.CommonEntity;
import icu.lowcoder.spring.cloud.organization.dao.AuthorityRepository;
import icu.lowcoder.spring.cloud.organization.entity.Authority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jws.Oneway;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PositionManager {

    @Autowired
    private AuthorityRepository authorityRepository;

    /**
     * 返回职位对应权限id列表
     * @param positionCodes 职位代码
     * @return
     */
    public List<UUID> positionsAuthorities(String... positionCodes) {
        // 加载职位权限
        List<UUID> authoritiesIds = new ArrayList<>();
        if (positionCodes != null) {
            List<Authority> authorities = authorityRepository.findAllByPositionsCodeIn(
                    Stream.of(positionCodes)
                    .map(String::toUpperCase)
                    .distinct()
                    .collect(Collectors.toList())
            );

            if (authorities.isEmpty()) {
                return authoritiesIds;
            }

            authoritiesIds.addAll(authorities.stream().map(CommonEntity::getId).collect(Collectors.toList()));
        }

        return authoritiesIds;
    }

}
