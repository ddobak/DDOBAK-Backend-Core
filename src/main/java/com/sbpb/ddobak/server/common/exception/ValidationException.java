package com.sbpb.ddobak.server.common.exception;

/**
 * 유효성 검증 실패 시 발생하는 예외
 * 
 * 사용 시나리오:
 * - 비즈니스 로직에서의 커스텀 검증 실패
 * - Bean Validation으로 처리하기 어려운 복잡한 검증 로직
 * - 도메인 규칙 위반
 */
public class ValidationException extends BusinessException {

    /**
     * 기본 검증 실패 예외
     * 
     * @param message 검증 실패 메시지
     */
    public ValidationException(String message) {
        super(CommonErrorCode.INVALID_INPUT, message);
    }

    /**
     * 특정 필드의 검증 실패 예외
     * 
     * @param field  검증 실패한 필드명
     * @param value  실패한 값
     * @param reason 실패 이유
     */
    public ValidationException(String field, Object value, String reason) {
        super(CommonErrorCode.INVALID_INPUT,
                String.format("Validation failed for field '%s' with value '%s': %s", field, value, reason));
        addProperty("field", field);
        addProperty("value", value);
        addProperty("reason", reason);
    }

    /**
     * 여러 필드의 검증 실패를 한 번에 처리
     * 
     * @param validationErrors 검증 오류 목록 (필드명=오류메시지 형태)
     */
    public ValidationException(java.util.Map<String, String> validationErrors) {
        super(CommonErrorCode.INVALID_INPUT, "Multiple validation errors occurred");
        addProperty("errors", validationErrors);
    }

    /**
     * 비즈니스 규칙 위반 예외
     * 
     * @param ruleName    위반된 규칙명
     * @param description 규칙 설명
     */
    public static ValidationException businessRuleViolation(String ruleName, String description) {
        ValidationException exception = new ValidationException(
                String.format("Business rule violation: %s - %s", ruleName, description));
        exception.addProperty("rule", ruleName);
        exception.addProperty("description", description);
        return exception;
    }
}