package icu.lowcoder.spring.cloud.content.dao;

import icu.lowcoder.spring.cloud.content.entity.BannerTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface BannerTagRepository extends JpaRepository<BannerTag, UUID>, JpaSpecificationExecutor<BannerTag> {
    List<BannerTag> findAllByNameIn(Set<String> tagNames);
}
