package com.sbpb.ddobak.server.common.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.sbpb.ddobak.server.common.exception.ErrorCode;

/**
 * API 공통 응답 형식
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("code")
    private int code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("userMessage")
    private String userMessage;

    @JsonProperty("data")
    private T data;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;

    @JsonProperty("traceId")
    private String traceId;

    // ===== 성공 응답 팩토리 메서드들 =====

    /**
     * 도메인별 성공 응답 생성 (데이터 포함)
     */
    public static <T, S extends SuccessCode> ApiResponse<T> success(T data, S successCode) {
        return new ApiResponse<>(
                true,
                successCode.getCode(),
                successCode.getMessage(),
                successCode.getMessage(),
                data,
                LocalDateTime.now(),
                generateTraceId());
    }

    /**
     * 도메인별 성공 응답 생성 (데이터 없음)
     */
    public static <T, S extends SuccessCode> ApiResponse<T> success(S successCode) {
        return new ApiResponse<>(
                true,
                successCode.getCode(),
                successCode.getMessage(),
                successCode.getMessage(),
                null,
                LocalDateTime.now(),
                generateTraceId());
    }

    // ===== 실패 응답 팩토리 메서드들 =====

    /**
     * 도메인별 에러 응답 생성
     */
    public static <T, E extends ErrorCode> ApiResponse<T> error(E errorCode) {
        return new ApiResponse<>(
                false,
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getMessage(),
                null,
                LocalDateTime.now(),
                generateTraceId());
    }

    /**
     * 도메인별 커스텀 에러 응답 생성
     */
    public static <T, E extends ErrorCode> ApiResponse<T> error(E errorCode, String customMessage) {
        return new ApiResponse<>(
                false,
                errorCode.getCode(),
                customMessage,
                errorCode.getMessage(),
                null,
                LocalDateTime.now(),
                generateTraceId());
    }

    // ===== 유틸리티 메서드들 =====

    /**
     * traceID 생성
     */
    private static String generateTraceId() {
        return java.util.UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return String.format("ApiResponse{success=%s, code=%d, message='%s', traceId='%s'}",
                success, code, message, traceId);
    }
}