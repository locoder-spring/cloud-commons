package icu.lowcoder.spring.cloud.content.entity;

import icu.lowcoder.spring.commons.jpa.auditing.AuditingEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.UUID;

/**
 * @author  yanhan
 * description:
 * date:  create in 2021/3/1 10:27 上午
 */
@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SQLDelete(sql = "update banner_image set deleted = true where id = ? ")
@Where(clause = "deleted = false")
public class BannerImage extends AuditingEntity {

    /**文件id*/
    private UUID fileId;
    /**图片名称*/
    private String name;
    /**路径*/
    private String path;

    @OneToOne
    private Banner banner;

    private Boolean deleted = false;
    @Id
    @GeneratedValue(generator = "bannerImageIdGenerator")
    private UUID id;
}
