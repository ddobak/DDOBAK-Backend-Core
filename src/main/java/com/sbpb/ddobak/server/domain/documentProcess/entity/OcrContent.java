package com.sbpb.ddobak.server.domain.documentProcess.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ocr_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OcrContent {

    @Id
    private String id;

    @Column(name = "contract_id", nullable = false)
    private String contractId;

    @Column(name = "content", columnDefinition = "TEXT")
    @Setter
    private String content;

    @Column(name = "category")
    @Setter
    private String category;

    @Column(name = "tag_idx")
    @Setter
    private Integer tagIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    @Setter
    private Contract contract;

    public OcrContent(String id, String contractId, String content, String category, Integer tagIdx) {
        this.id = id;
        this.contractId = contractId;
        this.content = content;
        this.category = category;
        this.tagIdx = tagIdx;
    }
} 