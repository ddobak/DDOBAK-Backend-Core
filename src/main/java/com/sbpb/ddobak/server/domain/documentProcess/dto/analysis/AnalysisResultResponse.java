package com.sbpb.ddobak.server.domain.documentProcess.dto.analysis;

import java.time.LocalDateTime;
import java.util.List;

public class AnalysisResultResponse {
    
    private String originContent;
    private String summary;
    private String analysisStatus;
    private LocalDateTime analysisDate;
    private long toxicCount;
    private DdobakCommentary ddobakCommentary;
    private List<ToxicClauseDto> toxics;

    public AnalysisResultResponse() {
    }

    public String getOriginContent() {
        return originContent;
    }

    public void setOriginContent(String originContent) {
        this.originContent = originContent;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(String analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public LocalDateTime getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(LocalDateTime analysisDate) {
        this.analysisDate = analysisDate;
    }

    public long getToxicCount() {
        return toxicCount;
    }

    public void setToxicCount(long toxicCount) {
        this.toxicCount = toxicCount;
    }

    public DdobakCommentary getDdobakCommentary() {
        return ddobakCommentary;
    }

    public void setDdobakCommentary(DdobakCommentary ddobakCommentary) {
        this.ddobakCommentary = ddobakCommentary;
    }

    public List<ToxicClauseDto> getToxics() {
        return toxics;
    }

    public void setToxics(List<ToxicClauseDto> toxics) {
        this.toxics = toxics;
    }

    public static class DdobakCommentary {
        private String overallComment;
        private String warningComment;
        private String advice;

        public DdobakCommentary() {
        }

        public DdobakCommentary(String overallComment, String warningComment, String advice) {
            this.overallComment = overallComment;
            this.warningComment = warningComment;
            this.advice = advice;
        }

        public String getOverallComment() {
            return overallComment;
        }

        public void setOverallComment(String overallComment) {
            this.overallComment = overallComment;
        }

        public String getWarningComment() {
            return warningComment;
        }

        public void setWarningComment(String warningComment) {
            this.warningComment = warningComment;
        }

        public String getAdvice() {
            return advice;
        }

        public void setAdvice(String advice) {
            this.advice = advice;
        }
    }

    public static class ToxicClauseDto {
        private String title;
        private String clause;
        private String reason;
        private String reasonReference;
        private int warnLevel;

        public ToxicClauseDto() {
        }

        public ToxicClauseDto(String title, String clause, String reason, String reasonReference, int warnLevel) {
            this.title = title;
            this.clause = clause;
            this.reason = reason;
            this.reasonReference = reasonReference;
            this.warnLevel = warnLevel;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getClause() {
            return clause;
        }

        public void setClause(String clause) {
            this.clause = clause;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getReasonReference() {
            return reasonReference;
        }

        public void setReasonReference(String reasonReference) {
            this.reasonReference = reasonReference;
        }

        public int getWarnLevel() {
            return warnLevel;
        }

        public void setWarnLevel(int warnLevel) {
            this.warnLevel = warnLevel;
        }
    }
} 