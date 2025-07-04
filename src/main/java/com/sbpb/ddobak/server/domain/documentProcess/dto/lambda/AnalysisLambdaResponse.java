package com.sbpb.ddobak.server.domain.documentProcess.dto.lambda;

import java.util.List;

/**
 * Analysis Lambda 응답 DTO
 */
public class AnalysisLambdaResponse {
    
    private boolean success;
    private String message;
    private AnalysisData data;

    public AnalysisLambdaResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AnalysisData getData() {
        return data;
    }

    public void setData(AnalysisData data) {
        this.data = data;
    }

    public static class AnalysisData {
        private String summary;
        private DdobakCommentary ddobakCommentary;
        private List<ToxicClause> toxics;

        public AnalysisData() {
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public DdobakCommentary getDdobakCommentary() {
            return ddobakCommentary;
        }

        public void setDdobakCommentary(DdobakCommentary ddobakCommentary) {
            this.ddobakCommentary = ddobakCommentary;
        }

        public List<ToxicClause> getToxics() {
            return toxics;
        }

        public void setToxics(List<ToxicClause> toxics) {
            this.toxics = toxics;
        }
    }

    public static class DdobakCommentary {
        private String overallComment;
        private String warningComment;
        private String advice;

        public DdobakCommentary() {
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

    public static class ToxicClause {
        private String title;
        private String clause;
        private String reason;
        private String reasonReference;
        private int warnLevel;

        public ToxicClause() {
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