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
