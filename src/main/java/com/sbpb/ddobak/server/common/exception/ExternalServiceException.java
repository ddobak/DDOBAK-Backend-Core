package com.sbpb.ddobak.server.common.exception;

/**
 * 외부 서비스 연동 시 발생하는 예외
 * 
 * 사용 시나리오:
 * - 외부 API 호출 실패
 * - 외부 서비스 응답 타임아웃
 * - 외부 서비스 인증/인가 실패
 * - 서킷 브레이커 동작
 */
public class ExternalServiceException extends BusinessException {

    /**
     * 기본 외부 서비스 예외
     */
    public ExternalServiceException(String message) {
        super(CommonErrorCode.EXTERNAL_SERVICE_ERROR, message);
    }

    /**
     * 서비스명과 이유를 명시한 예외
     */
    public ExternalServiceException(String serviceName, String reason) {
        super(CommonErrorCode.EXTERNAL_SERVICE_ERROR,
                String.format("External service '%s' error: %s", serviceName, reason));
        addProperty("serviceName", serviceName);
        addProperty("reason", reason);
    }

    /**
     * 에러 코드를 직접 지정하는 예외
     */
    public ExternalServiceException(ErrorCode errorCode, String serviceName, String reason) {
        super(errorCode, String.format("External service '%s' error: %s", serviceName, reason));
        addProperty("serviceName", serviceName);
        addProperty("reason", reason);
    }
}