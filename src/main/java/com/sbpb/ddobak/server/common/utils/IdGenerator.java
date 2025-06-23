package com.sbpb.ddobak.server.common.utils;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * 짧고 읽기 쉬운 ID 생성 유틸리티
 */
public class IdGenerator {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String ALPHANUMERIC_LOWERCASE = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 범용 ID 생성 (8자리)
     * 기본 엔티티 ID 생성용
     */
    public static String generate() {
        return generateEntityId();
    }

    /**
     * 비즈니스 엔티티용 8자리 ID 생성 (대문자 + 숫자)
     * 예: C7X9K2M1, U3H8N5Q7
     */
    public static String generateEntityId() {
        return generateRandomString(ALPHANUMERIC, 8);
    }

    /**
     * 특정 접두사가 있는 엔티티 ID 생성
     * 예: 'C' + 7자리 = C7X9K2M1
     */
    public static String generateEntityId(String prefix) {
        return prefix.toUpperCase() + generateRandomString(ALPHANUMERIC, 7);
    }

    /**
     * Trace ID용 12자리 ID 생성 (소문자 + 숫자)
     * 예: a1b2c3d4e5f6
     */
    public static String generateTraceId() {
        return generateRandomString(ALPHANUMERIC_LOWERCASE, 12);
    }

    /**
     * 사용자 ID용 짧은 ID 생성 (8자리)
     * 예: U7X9K2M1
     */
    public static String generateUserId() {
        return generateEntityId("U");
    }

    /**
     * 계약서 ID용 짧은 ID 생성 (8자리)
     * 예: C7X9K2M1
     */
    public static String generateContractId() {
        return generateEntityId("C");
    }

    /**
     * 분석 ID용 짧은 ID 생성 (8자리)
     * 예: A7X9K2M1
     */
    public static String generateAnalysisId() {
        return generateEntityId("A");
    }

    /**
     * OCR 결과 ID용 짧은 ID 생성 (8자리)
     * 예: O7X9K2M1
     */
    public static String generateOcrResultId() {
        return generateEntityId("O");
    }

    /**
     * 독소 조항 ID용 짧은 ID 생성 (8자리)
     * 예: T7X9K2M1
     */
    public static String generateToxicClauseId() {
        return generateEntityId("T");
    }

    /**
     * 임시 파일명용 ID 생성 (16자리)
     * 예: a1b2c3d4e5f6g7h8
     */
    public static String generateTempFileId() {
        return generateRandomString(ALPHANUMERIC_LOWERCASE, 16);
    }

    /**
     * 시간 기반 ID 생성 (테스트/디버깅용)
     * 예: 20240115103000ABC (연월일시분초 + 3자리 랜덤)
     */
    public static String generateTimestampId() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String random = generateRandomString(ALPHANUMERIC, 3);
        return timestamp + random;
    }

    /**
     * 지정된 문자셋과 길이로 랜덤 문자열 생성
     */
    private static String generateRandomString(String charset, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(charset.charAt(RANDOM.nextInt(charset.length())));
        }
        return sb.toString();
    }

    /**
     * ID 유효성 검증 (8자리 영숫자)
     */
    public static boolean isValidEntityId(String id) {
        return id != null && id.length() == 8 && id.matches("[A-Z0-9]{8}");
    }

    /**
     * 접두사가 있는 ID 유효성 검증
     */
    public static boolean isValidEntityId(String id, String expectedPrefix) {
        return id != null && 
               id.length() == 8 && 
               id.startsWith(expectedPrefix.toUpperCase()) && 
               id.matches("[A-Z0-9]{8}");
    }
} 