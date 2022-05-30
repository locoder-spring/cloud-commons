package icu.lowcoder.spring.cloud.message.entity;

import icu.lowcoder.spring.commons.jpa.auditing.AuditingEntity;
import icu.lowcoder.spring.cloud.message.push.PushChannel;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Entity
@Getter
@Setter
@SQLDelete(sql = "update message set deleted = true where id = ? and ? != -999  ")
@Where(clause = "deleted = false")
@TypeDef(name = "jsonObject", typeClass = JsonBinaryType.class)
public class Message extends AuditingEntity {
    private UUID accountId;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Column(columnDefinition = "TEXT")
    private String link; // 链接

    @ManyToMany
    @JoinTable(
            name = "message_tags",
            joinColumns = {
                    @JoinColumn(name = "message_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "tag_id")
            }
    )
    private List<MessageTag> tags = new ArrayList<>();

    @ColumnDefault("false")
    private Boolean broadcast = false; // 广播消息
    @ColumnDefault("false")
    private Boolean quiet = false; // 静默消息
    @ColumnDefault("false")
    private Boolean visible = true; // 可见

    private Date sendTime; // 发送时间
    private Date submitTime; // 提交时间


    @ColumnDefault("false")
    private Boolean sent = false; // 已发
    @ColumnDefault("false")
    private Boolean inQueue = false; // 在发送队列中

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Type(type = "jsonObject")
    @Column(columnDefinition = "jsonb")
    private List<PushChannel> pushChannels;

    @Where(clause = "deleted = false")
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageReadRecord> readRecords = new ArrayList<>();

    @Where(clause = "deleted = false")
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessagePushRecord> pushRecords = new ArrayList<>();

    @ColumnDefault("false")
    private Boolean deleted = false;

    @Version
    @ColumnDefault("0")
    private Long version = 0L;
}
