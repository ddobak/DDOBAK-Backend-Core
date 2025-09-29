package com.sbpb.ddobak.server.domain.documentProcess.exception;

import com.sbpb.ddobak.server.common.exception.BusinessException;

/**
 * 계약서 접근 권한이 없을 때 발생하는 예외
 */
public class ContractAccessDeniedException extends BusinessException {
    
    /**
     * 기본 생성자
     */
    public ContractAccessDeniedException() {
        super(DocumentProcessErrorCode.CONTRACT_OWNER_MISMATCH);
    }
    
    /**
     * 커스텀 메시지를 사용하는 생성자
     * 
     * @param message 상세 메시지
     */
    public ContractAccessDeniedException(String message) {
        super(DocumentProcessErrorCode.CONTRACT_OWNER_MISMATCH, message);
    }
    
    /**
     * 계약서 ID와 사용자 ID로 예외 생성
     * 
     * @param contractId 계약서 ID
     * @param userId 사용자 ID
     */
    public ContractAccessDeniedException(String contractId, Long userId) {
        super(DocumentProcessErrorCode.CONTRACT_OWNER_MISMATCH, 
              String.format("사용자 %d는 계약서 %s에 접근할 권한이 없습니다", userId, contractId));
        addProperty("contractId", contractId);
        addProperty("userId", userId);
    }
}
