package com.sbpb.ddobak.server.domain.documentProcess.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OCR 컨텐츠 엔티티
 */
@Entity
@Table(name = "ocr_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OcrContent {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "contract_id", nullable = false)
    private String contractId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "tag_idx", nullable = false)
    private Integer tagIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    private Contract contract;

    @Builder
    public OcrContent(String id, String contractId, String content, Integer tagIdx) {
        this.id = id;
        this.contractId = contractId;
        this.content = content;
        this.tagIdx = tagIdx;
    }

    /**
     * OCR 컨텐츠 업데이트
     */
    public void updateContent(String content) {
        this.content = content;
    }
} 