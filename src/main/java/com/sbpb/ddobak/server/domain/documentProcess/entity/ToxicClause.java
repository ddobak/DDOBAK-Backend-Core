package com.sbpb.ddobak.server.domain.documentProcess.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "toxic_clauses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToxicClause {

    @Id
    private String id;

    @Column(name = "analysis_id", nullable = false)
    private String analysisId;

    @Column(name = "title")
    @Setter
    private String title;

    @Column(name = "clause", columnDefinition = "TEXT")
    @Setter
    private String clause;

    @Column(name = "reason", columnDefinition = "TEXT")
    @Setter
    private String reason;

    @Column(name = "reason_reference", columnDefinition = "TEXT")
    @Setter
    private String reasonReference;

    @Column(name = "source_contract_tag_idx")
    @Setter
    private Integer sourceContractTagIdx;

    @Column(name = "warn_level")
    @Setter
    private Integer warnLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", insertable = false, updatable = false)
    @Setter
    private ContractAnalysis contractAnalysis;

    public ToxicClause(String id, String analysisId, String title, String clause, String reason, 
                       String reasonReference, Integer sourceContractTagIdx, Integer warnLevel) {
        this.id = id;
        this.analysisId = analysisId;
        this.title = title;
        this.clause = clause;
        this.reason = reason;
        this.reasonReference = reasonReference;
        this.sourceContractTagIdx = sourceContractTagIdx;
        this.warnLevel = warnLevel;
    }
} 