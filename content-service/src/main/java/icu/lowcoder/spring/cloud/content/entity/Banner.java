package icu.lowcoder.spring.cloud.content.entity;

import icu.lowcoder.spring.commons.jpa.auditing.AuditingEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author  yanhan
 * description:
 * date:  create in 2021/3/1 10:23 上午
 */
@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SQLDelete(sql = "update banner set deleted = true where id = ? ")
@Where(clause = "deleted = false")
public class Banner extends AuditingEntity {

    /**名称*/
    private String name;

    /**排序*/
    private byte sequence;

    /**图片*/
    @OneToOne(mappedBy = "banner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private BannerImage image;

    /**跳转描述*/
    @OneToOne(mappedBy = "banner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private BannerJump jump;

    /**展示开始时间*/
    private Date beginDate;

    /**展示结束时间*/
    private Date endDate;


    /**是否启用*/
    private	boolean enabled;

    @ManyToMany
    @JoinTable(
            name = " banner_tags",
            joinColumns = {
                    @JoinColumn(name = "banner_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "tag_id")
            }
    )
    private List<BannerTag> tags = new ArrayList<>();

    private Boolean deleted = false;
    @Id
    @GeneratedValue(generator = "bannerIdGenerator")
    private UUID id;
}
