package com.sbpb.ddobak.server.domain.documentProcess.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contract_analyses")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContractAnalysis {

    @Id
    private String id;

    @Column(name = "contract_id", nullable = false)
    private String contractId;

    @Column(name = "summary", columnDefinition = "TEXT")
    @Setter
    private String summary;

    @Column(name = "status")
    @Setter
    private String status;

    @Column(name = "ddobak_overall_comment")
    @Setter
    private String ddobakOverallComment;

    @Column(name = "ddobak_warning_comment")
    @Setter
    private String ddobakWarningComment;

    @Column(name = "ddobak_advice")
    @Setter
    private String ddobakAdvice;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_status")
    @Setter
    private ProcessStatus processStatus;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    @Setter
    private Contract contract;

    @OneToMany(mappedBy = "contractAnalysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToxicClause> toxicClauses = new ArrayList<>();

    public ContractAnalysis(String id, String contractId, String summary, String status) {
        this.id = id;
        this.contractId = contractId;
        this.summary = summary;
        this.status = status;
        this.processStatus = ProcessStatus.IN_PROGRESS;
    }

    public void addToxicClause(ToxicClause toxicClause) {
        this.toxicClauses.add(toxicClause);
        toxicClause.setContractAnalysis(this);
    }

    public enum ProcessStatus {
        IN_PROGRESS, COMPLETED, FAILED
    }
} 