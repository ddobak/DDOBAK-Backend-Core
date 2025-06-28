package com.sbpb.ddobak.server.common.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sbpb.ddobak.server.common.exception.ErrorCode;
import com.sbpb.ddobak.server.common.utils.IdGenerator;
import lombok.Getter;
import lombok.Setter;

/**
 * 모든 API 응답에 사용되는 통일된 응답 형식
 * 
 * 특징:
 * - 성공/실패 여부를 명확히 표시
 * - 일관된 에러 코드 체계 사용
 * - 추적 가능한 응답 (traceId)
 * - 타입 안전성 보장 (Generic 사용)
 * - null 값 제외 (Jackson)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ApiResponse<T> {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("code")
    private int code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @JsonProperty("trace_id")
    private String traceId;

    // 기본 생성자 (Jackson 직렬화용)
    public ApiResponse() {
    }

    // 전체 필드 생성자
    private ApiResponse(boolean success, int code, String message, T data, LocalDateTime timestamp, String traceId) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.traceId = traceId;
    }

    // ===== 성공 응답 팩토리 메서드들 =====

    /**
     * 기본 성공 응답 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                SuccessCode.SUCCESS.getCode(),
                SuccessCode.SUCCESS.getMessage(),
                data,
                LocalDateTime.now(),
                generateTraceId());
    }

    /**
     * 커스텀 성공 코드를 사용한 성공 응답 (모든 도메인 성공 코드 지원)
     */
    public static <T> ApiResponse<T> success(T data, BaseSuccessCode successCode) {
        return new ApiResponse<>(
                true,
                successCode.getCode(),
                successCode.getMessage(),
                data,
                LocalDateTime.now(),
                generateTraceId());
    }

    /**
     * 데이터 없는 성공 응답
     */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(
                true,
                SuccessCode.SUCCESS.getCode(),
                SuccessCode.SUCCESS.getMessage(),
                null,
                LocalDateTime.now(),
                generateTraceId());
    }

    /**
     * 커스텀 성공 코드를 사용한 데이터 없는 성공 응답 (모든 도메인 성공 코드 지원)
     */
    public static ApiResponse<Void> success(BaseSuccessCode successCode) {
        return new ApiResponse<>(
                true,
                successCode.getCode(),
                successCode.getMessage(),
                null,
                LocalDateTime.now(),
                generateTraceId());
    }

    // ===== 실패 응답 팩토리 메서드들 =====

    /**
     * 에러 코드를 사용한 실패 응답
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(
                false,
                errorCode.getCode(),
                errorCode.getMessage(),
                null,
                LocalDateTime.now(),
                generateTraceId());
    }

    /**
     * 커스텀 메시지를 사용한 실패 응답
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String customMessage) {
        return new ApiResponse<>(
                false,
                errorCode.getCode(),
                customMessage,
                null,
                LocalDateTime.now(),
                generateTraceId());
    }

    /**
     * 코드와 메시지를 직접 지정한 실패 응답 (특수한 경우에만 사용)
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(
                false,
                code,
                message,
                null,
                LocalDateTime.now(),
                generateTraceId());
    }

    // ===== 유틸리티 메서드들 =====

    /**
     * 추적 ID 생성 (12자리 짧은 ID)
     */
    private static String generateTraceId() {
        return IdGenerator.generateTraceId();
    }

    // ===== Getter/Setter 메서드들은 Lombok으로 자동 생성됨 =====

    /**
     * 디버깅용 toString 메서드
     */
    @Override
    public String toString() {
        return String.format("ApiResponse{success=%s, code=%d, message='%s', traceId='%s'}",
                success, code, message, traceId);
    }
}