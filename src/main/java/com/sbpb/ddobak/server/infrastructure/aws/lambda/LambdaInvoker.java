package com.sbpb.ddobak.server.infrastructure.aws.lambda;

import com.sbpb.ddobak.server.common.response.ApiResponse;

import java.util.Map;

/**
 * Lambda 함수 호출 포트 인터페이스
 * 
 * Clean Architecture 원칙:
 * - Infrastructure Layer에서 정의하는 외부 시스템 연동 포트
 * - Domain Layer에서 이 인터페이스에 의존
 * - 구현체는 동일한 Infrastructure Layer에서 제공
 */
public interface LambdaInvoker {

    /**
     * Lambda 함수 동기 호출
     * 
     * @param functionName 함수명
     * @param payload 요청 페이로드 (null 가능)
     * @return Lambda 함수 실행 결과
     */
    ApiResponse<Map<String, Object>> invoke(String functionName, Map<String, Object> payload);

    /**
     * Lambda 함수 동기 호출 (페이로드 없음)
     * 
     * @param functionName 함수명
     * @return Lambda 함수 실행 결과
     */
    default ApiResponse<Map<String, Object>> invoke(String functionName) {
        return invoke(functionName, null);
    }

    /**
     * Lambda 함수 비동기 호출
     * 
     * @param functionName 함수명
     * @param payload 요청 페이로드 (null 가능)
     * @return 비동기 호출 상태 (큐 등록 여부)
     */
    ApiResponse<Map<String, Object>> invokeAsync(String functionName, Map<String, Object> payload);

    /**
     * Lambda 함수 비동기 호출 (페이로드 없음)
     * 
     * @param functionName 함수명
     * @return 비동기 호출 상태 (큐 등록 여부)
     */
    default ApiResponse<Map<String, Object>> invokeAsync(String functionName) {
        return invokeAsync(functionName, null);
    }
} 