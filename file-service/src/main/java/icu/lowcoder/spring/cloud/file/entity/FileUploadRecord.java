package icu.lowcoder.spring.cloud.file.entity;

import icu.lowcoder.spring.commons.jpa.auditing.AuditingEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Getter
@Setter
@Entity
@SQLDelete(sql = "update file_upload_record set deleted = true where id = ? ")
@Where(clause = "deleted = false")
public class FileUploadRecord extends AuditingEntity {

    @Column(nullable = false)
    private String userId;

    private Long size;

    private String extension;

    private String type;

    private String originalFilename;

    private String name;

    private String path;

    private String bucket;

    private String fullKey;

    // 逻辑删除
    @ColumnDefault("false")
    private Boolean deleted = false;
}
