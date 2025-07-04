package com.sbpb.ddobak.server.common.exception;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 * 
 * 사용 시나리오:
 * - 데이터베이스에서 특정 ID로 엔티티 조회 실패
 * - 파일이나 외부 리소스 접근 실패
 * - API 엔드포인트에서 요청한 리소스가 존재하지 않음
 */
public class ResourceNotFoundException extends BusinessException {

    /**
     * 기본 리소스 없음 예외
     * 
     * @param message 리소스를 찾을 수 없다는 메시지
     */
    public ResourceNotFoundException(String message) {
        super(CommonErrorCode.RESOURCE_NOT_FOUND, message);
    }

    /**
     * 특정 리소스 타입과 ID로 리소스 없음 예외
     * 
     * @param resourceType 리소스 타입 (예: "User", "Product")
     * @param resourceId   리소스 ID
     */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(CommonErrorCode.RESOURCE_NOT_FOUND,
                String.format("%s with id '%s' not found", resourceType, resourceId));
        addProperty("resourceType", resourceType);
        addProperty("resourceId", resourceId);
    }

    /**
     * 특정 조건으로 리소스를 찾을 수 없을 때
     * 
     * @param resourceType 리소스 타입
     * @param field        검색 필드
     * @param value        검색 값
     */
    public ResourceNotFoundException(String resourceType, String field, Object value) {
        super(CommonErrorCode.RESOURCE_NOT_FOUND,
                String.format("%s with %s '%s' not found", resourceType, field, value));
        addProperty("resourceType", resourceType);
        addProperty("field", field);
        addProperty("value", value);
    }

    /**
     * 사용자 리소스 없음 예외 (편의 메서드)
     * 
     * @param userId 사용자 ID
     */
    public static ResourceNotFoundException user(Long userId) {
        return new ResourceNotFoundException("User", "id", userId);
    }

    /**
     * 이메일로 사용자를 찾을 수 없는 경우 (편의 메서드)
     * 
     * @param email 이메일 주소
     */
    public static ResourceNotFoundException userByEmail(String email) {
        return new ResourceNotFoundException("User", "email", email);
    }

    /**
     * 문서 리소스 없음 예외 (편의 메서드)
     * 
     * @param documentId 문서 ID
     */
    public static ResourceNotFoundException document(Long documentId) {
        return new ResourceNotFoundException("Document", "id", documentId);
    }

    /**
     * 외부 컨텐츠 리소스 없음 예외 (편의 메서드)
     * 
     * @param contentId 컨텐츠 ID
     */
    public static ResourceNotFoundException externalContent(Long contentId) {
        return new ResourceNotFoundException("External Content", "id", contentId);
    }

    /**
     * 파일 리소스 없음 예외 (편의 메서드)
     * 
     * @param fileName 파일명
     */
    public static ResourceNotFoundException file(String fileName) {
        return new ResourceNotFoundException("File", "name", fileName);
    }
}