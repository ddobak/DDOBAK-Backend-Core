package com.sbpb.ddobak.server.infrastructure.aws.stepfunctions;

import com.sbpb.ddobak.server.common.response.ApiResponse;

import java.util.Map;

/**
 * Step Functions State Machine 실행 포트 인터페이스
 * 
 * Clean Architecture 원칙:
 * - Infrastructure Layer에서 정의하는 외부 시스템 연동 포트
 * - Domain Layer에서 이 인터페이스에 의존
 * - 구현체는 동일한 Infrastructure Layer에서 제공
 */
public interface StepFunctionsInvoker {

    /**
     * State Machine 실행 시작 (비동기)
     * 
     * @param stateMachineName State Machine 이름 (설정에서 조회)
     * @param input 실행 입력 데이터
     * @return 실행 결과 (실행 ARN 포함)
     */
    ApiResponse<Map<String, Object>> startExecution(String stateMachineName, Map<String, Object> input);

    /**
     * State Machine 실행 시작 (입력 없음, 비동기)
     * 
     * @param stateMachineName State Machine 이름
     * @return 실행 결과 (실행 ARN 포함)
     */
    default ApiResponse<Map<String, Object>> startExecution(String stateMachineName) {
        return startExecution(stateMachineName, null);
    }

    /**
     * State Machine 동기 실행 (완료까지 대기)
     * Express 워크플로우에서만 사용 가능
     * 
     * @param stateMachineName State Machine 이름
     * @param input 실행 입력 데이터
     * @return 실행 완료 결과 (출력 데이터 포함)
     */
    ApiResponse<Map<String, Object>> startSyncExecution(String stateMachineName, Map<String, Object> input);

    /**
     * State Machine 동기 실행 (입력 없음)
     * 
     * @param stateMachineName State Machine 이름
     * @return 실행 완료 결과
     */
    default ApiResponse<Map<String, Object>> startSyncExecution(String stateMachineName) {
        return startSyncExecution(stateMachineName, null);
    }

    /**
     * State Machine 실행 상태 조회
     * 
     * @param executionArn 실행 ARN
     * @return 실행 상태 정보
     */
    ApiResponse<Map<String, Object>> describeExecution(String executionArn);

    /**
     * State Machine 실행 히스토리 조회
     * 
     * @param executionArn 실행 ARN
     * @return 실행 히스토리 목록
     */
    ApiResponse<Map<String, Object>> getExecutionHistory(String executionArn);

    /**
     * State Machine 실행 중단
     * 
     * @param executionArn 실행 ARN
     * @param reason 중단 사유
     * @return 중단 결과
     */
    ApiResponse<Map<String, Object>> stopExecution(String executionArn, String reason);

    /**
     * State Machine 실행 중단 (기본 사유)
     * 
     * @param executionArn 실행 ARN
     * @return 중단 결과
     */
    default ApiResponse<Map<String, Object>> stopExecution(String executionArn) {
        return stopExecution(executionArn, "Stopped by user request");
    }
} 