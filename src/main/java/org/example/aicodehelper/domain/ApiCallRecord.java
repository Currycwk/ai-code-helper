package org.example.aicodehelper.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 调用审计实体。
 * 对应 api_call_record 表，记录一次模型调用的请求摘要、响应摘要、
 * 字符统计、耗时、成功状态和错误信息，供用户统计和管理员审计使用。
 */
@Getter
@Setter
@Entity
@Table(name = "api_call_record")
public class ApiCallRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false, length = 64)
    private String modelName;

    @Column
    private Long memoryId;

    @Column(nullable = false)
    private Integer requestLength;

    @Column(nullable = false)
    private Integer responseLength;

    @Column(length = 255)
    private String requestPreview;

    @Column(length = 255)
    private String responsePreview;

    @Column(nullable = false)
    private Long latencyMs;

    @Column(nullable = false)
    private Boolean success;

    @Column(length = 512)
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
