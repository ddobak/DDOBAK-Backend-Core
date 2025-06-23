package com.sbpb.ddobak.server.domain.documentProcess.exception;

/**
 * Contract 관련 예외들
 */
public class ContractExceptions {

    /**
     * 계약서를 찾을 수 없는 경우
     */
    public static class ContractNotFoundException extends DocumentProcessBusinessException {
        public ContractNotFoundException(String contractId) {
            super(DocumentProcessErrorCode.CONTRACT_NOT_FOUND, "Contract not found with ID: " + contractId);
        }

        public static ContractNotFoundException withId(String contractId) {
            return new ContractNotFoundException(contractId);
        }
    }

    /**
     * 권한 없는 계약서 접근
     */
    public static class UnauthorizedContractAccessException extends DocumentProcessBusinessException {
        public UnauthorizedContractAccessException(String contractId) {
            super(DocumentProcessErrorCode.UNAUTHORIZED_CONTRACT_ACCESS, "Unauthorized access to contract: " + contractId);
        }

        public static UnauthorizedContractAccessException forContract(String contractId) {
            return new UnauthorizedContractAccessException(contractId);
        }
    }

    /**
     * 분석용 파일 관련 예외들
     */
    public static class AnalysisFileException extends DocumentProcessBusinessException {
        private AnalysisFileException(DocumentProcessErrorCode errorCode, String message) {
            super(errorCode, message);
        }

        public static AnalysisFileException fileMissing() {
            return new AnalysisFileException(DocumentProcessErrorCode.ANALYSIS_FILE_MISSING, "File is required for analysis processing");
        }

        public static AnalysisFileException fileTooLarge() {
            return new AnalysisFileException(DocumentProcessErrorCode.ANALYSIS_FILE_TOO_LARGE, "File size exceeds 20MB limit");
        }

        public static AnalysisFileException unsupportedFileType(String contentType) {
            return new AnalysisFileException(DocumentProcessErrorCode.ANALYSIS_UNSUPPORTED_FILE_TYPE, "Unsupported file type: " + contentType);
        }
    }

    /**
     * 분석 결과를 찾을 수 없는 경우
     */
    public static class AnalysisResultNotFoundException extends DocumentProcessBusinessException {
        public AnalysisResultNotFoundException(String contractId) {
            super(DocumentProcessErrorCode.ANALYSIS_RESULT_NOT_FOUND, "Analysis result not found for contract ID: " + contractId);
        }

        public static AnalysisResultNotFoundException forContract(String contractId) {
            return new AnalysisResultNotFoundException(contractId);
        }
    }

    /**
     * OCR 컨텐츠를 찾을 수 없는 경우
     */
    public static class OcrContentNotFoundException extends DocumentProcessBusinessException {
        public OcrContentNotFoundException(String contractId, Integer pageIndex) {
            super(DocumentProcessErrorCode.OCR_CONTENT_NOT_FOUND, 
                  String.format("OCR content not found for contract ID: %s, page: %d", contractId, pageIndex));
        }

        public static OcrContentNotFoundException forContractAndPage(String contractId, Integer pageIndex) {
            return new OcrContentNotFoundException(contractId, pageIndex);
        }
    }

    // 편의 메서드들
    public static ContractNotFoundException contractNotFound(String contractId) {
        return ContractNotFoundException.withId(contractId);
    }

    public static OcrContentNotFoundException ocrContentNotFound(String contractId, Integer pageIndex) {
        return OcrContentNotFoundException.forContractAndPage(contractId, pageIndex);
    }

    public static UnauthorizedContractAccessException unauthorizedAccess(String contractId) {
        return UnauthorizedContractAccessException.forContract(contractId);
    }

    public static AnalysisResultNotFoundException analysisResultNotFound(String contractId) {
        return AnalysisResultNotFoundException.forContract(contractId);
    }
} 