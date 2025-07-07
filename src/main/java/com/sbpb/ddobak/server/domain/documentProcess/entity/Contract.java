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
@Table(name = "contracts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contract {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "img_s3_key")
    @Setter
    private String imgS3Key;

    @Column(name = "title")
    @Setter
    private String title;

    @Column(name = "contract_type")
    @Enumerated(EnumType.STRING)
    @Setter
    private ContractType contractType;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OcrContent> ocrContents = new ArrayList<>();

    @OneToOne(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ContractAnalysis contractAnalysis;

    public Contract(String id, Long userId, String imgS3Key) {
        this.id = id;
        this.userId = userId;
        this.imgS3Key = imgS3Key;
    }

    public Contract(String id, Long userId, String imgS3Key, String title, ContractType contractType) {
        this.id = id;
        this.userId = userId;
        this.imgS3Key = imgS3Key;
        this.title = title;
        this.contractType = contractType;
    }

    public void addOcrContent(OcrContent ocrContent) {
        this.ocrContents.add(ocrContent);
        ocrContent.setContract(this);
    }

    public void setContractAnalysis(ContractAnalysis contractAnalysis) {
        this.contractAnalysis = contractAnalysis;
        if (contractAnalysis != null) {
            contractAnalysis.setContract(this);
        }
    }
} 