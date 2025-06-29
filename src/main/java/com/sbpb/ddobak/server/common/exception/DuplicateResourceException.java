package com.sbpb.ddobak.server.common.exception;

/**
 * 중복된 리소스 생성 시도 시 발생하는 예외
 * 
 * 사용 시나리오:
 * - 이미 존재하는 이메일로 회원가입 시도
 * - 중복된 이름이나 식별자로 리소스 생성 시도
 * - 유니크 제약조건 위반
 */
public class DuplicateResourceException extends BusinessException {

    /**
     * 기본 중복 리소스 예외
     * 
     * @param message 중복 리소스 메시지
     */
    public DuplicateResourceException(String message) {
        super(CommonErrorCode.DUPLICATE_RESOURCE, message);
    }

    /**
     * 특정 리소스 타입과 필드로 중복 예외
     * 
     * @param resourceType 리소스 타입 (예: "User", "Product")
     * @param field        중복된 필드
     * @param value        중복된 값
     */
    public DuplicateResourceException(String resourceType, String field, Object value) {
        super(CommonErrorCode.DUPLICATE_RESOURCE,
                String.format("%s with %s '%s' already exists", resourceType, field, value));
        addProperty("resourceType", resourceType);
        addProperty("field", field);
        addProperty("value", value);
    }

    /**
     * 특정 리소스 타입과 ID로 중복 예외
     * 
     * @param resourceType 리소스 타입
     * @param resourceId   리소스 ID
     */
    public DuplicateResourceException(String resourceType, Object resourceId) {
        super(CommonErrorCode.DUPLICATE_RESOURCE,
                String.format("%s with id '%s' already exists", resourceType, resourceId));
        addProperty("resourceType", resourceType);
        addProperty("resourceId", resourceId);
    }

    /**
     * 여러 필드가 복합적으로 중복된 경우
     * 
     * @param resourceType    리소스 타입
     * @param duplicateFields 중복된 필드들과 값들의 맵
     */
    public DuplicateResourceException(String resourceType, java.util.Map<String, Object> duplicateFields) {
        super(CommonErrorCode.DUPLICATE_RESOURCE,
                String.format("%s already exists with the provided fields", resourceType));
        addProperty("resourceType", resourceType);
        addProperty("duplicateFields", duplicateFields);
    }

    /**
     * 이메일 중복 예외 (편의 메서드)
     * 
     * @param email 중복된 이메일
     */
    public static DuplicateResourceException email(String email) {
        return new DuplicateResourceException("User", "email", email);
    }

    /**
     * 사용자명 중복 예외 (편의 메서드)
     * 
     * @param username 중복된 사용자명
     */
    public static DuplicateResourceException username(String username) {
        return new DuplicateResourceException("User", "username", username);
    }

    /**
     * 문서명 중복 예외 (편의 메서드)
     * 
     * @param documentName 중복된 문서명
     */
    public static DuplicateResourceException documentName(String documentName) {
        return new DuplicateResourceException("Document", "name", documentName);
    }

    /**
     * 컨텐츠 URL 중복 예외 (편의 메서드)
     * 
     * @param url 중복된 URL
     */
    public static DuplicateResourceException contentUrl(String url) {
        return new DuplicateResourceException("External Content", "url", url);
    }

    /**
     * 일반적인 키 중복 예외 (편의 메서드)
     * 
     * @param resourceType 리소스 타입
     * @param key          중복된 키
     */
    public static DuplicateResourceException key(String resourceType, String key) {
        return new DuplicateResourceException(resourceType, "key", key);
    }
}