package com.sbpb.ddobak.server.infrastructure.aws.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbpb.ddobak.server.infrastructure.aws.client.AwsClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.HashMap;
import java.util.Map;

/**
 * 테스트 환경 전용 AWS Infrastructure 설정
 * 
 * 특징:
 * - 테스트용 Lambda 함수 설정 (test_lambda) 포함
 * - 운영 설정과 분리하여 테스트 안정성 확보
 * - @Primary로 테스트 시 우선 적용
 */
@Slf4j
@TestConfiguration
@RequiredArgsConstructor
public class AwsTestConfig {

    private final AwsClientFactory awsClientFactory;

    /**
     * 테스트용 ObjectMapper Bean
     * JSON 직렬화/역직렬화를 위해 필요
     */
    @Bean
    @Primary
    public ObjectMapper testObjectMapper() {
        return new ObjectMapper();
    }

    /**
     * 테스트용 Lambda Client Bean
     */
    @Bean
    @Primary
    public LambdaClient testLambdaClient() {
        return awsClientFactory.createLambdaClient();
    }

    /**
     * 테스트용 S3 Client Bean
     */
    @Bean
    @Primary
    public S3Client testS3Client() {
        return awsClientFactory.createS3Client();
    }

    /**
     * 테스트용 Lambda 함수 설정 맵
     * test_lambda 설정을 운영 설정과 분리
     */
    @Bean
    @Primary
    public Map<String, LambdaFunctionSpec> testLambdaFunctions() {
        log.info("Initializing test Lambda function configurations");
        
        Map<String, LambdaFunctionSpec> functions = new HashMap<>();
        
        // Test Lambda 설정 - 테스트 전용 동기 함수
        functions.put("test_lambda", LambdaFunctionSpec.builder()
                .functionName("test_lambda")
                .description("테스트용 Lambda 함수 (동기)")
                .defaultInvocationType("RequestResponse") // 동기 호출 - 테스트에서 응답 확인 필요
                .timeoutMillis(10000L) // 10초 - 테스트용이므로 짧은 타임아웃
                .validateResponseSchema(false) // 응답 스키마 검증 비활성화 - 테스트용이므로 유연하게
                .maxRetries(1) // 재시도 1회 - 테스트용이므로 최소 재시도
                .collectLogs(false) // 로그 수집 비활성화 - 테스트 로그 간소화
                .build());
        
        log.info("Test Lambda function configurations initialized: {}", functions.keySet());
        return functions;
    }

    /**
     * 테스트용 Lambda 함수 스펙 정의
     * 운영용과 동일한 구조이지만 테스트에 특화된 설정 제공
     */
    public static class LambdaFunctionSpec {
        private String functionName;
        private String description;
        private String defaultInvocationType;
        private Long timeoutMillis;
        private boolean validateResponseSchema;
        private int maxRetries;
        private boolean collectLogs;

        // Builder 패턴
        public static LambdaFunctionSpecBuilder builder() {
            return new LambdaFunctionSpecBuilder();
        }

        // Getters
        public String getFunctionName() { return functionName; }
        public String getDescription() { return description; }
        public String getDefaultInvocationType() { return defaultInvocationType; }
        public Long getTimeoutMillis() { return timeoutMillis; }
        public boolean isValidateResponseSchema() { return validateResponseSchema; }
        public int getMaxRetries() { return maxRetries; }
        public boolean isCollectLogs() { return collectLogs; }

        // Builder 클래스
        public static class LambdaFunctionSpecBuilder {
            private String functionName;
            private String description;
            private String defaultInvocationType;
            private Long timeoutMillis;
            private boolean validateResponseSchema;
            private int maxRetries;
            private boolean collectLogs;

            public LambdaFunctionSpecBuilder functionName(String functionName) {
                this.functionName = functionName;
                return this;
            }

            public LambdaFunctionSpecBuilder description(String description) {
                this.description = description;
                return this;
            }

            public LambdaFunctionSpecBuilder defaultInvocationType(String defaultInvocationType) {
                this.defaultInvocationType = defaultInvocationType;
                return this;
            }

            public LambdaFunctionSpecBuilder timeoutMillis(Long timeoutMillis) {
                this.timeoutMillis = timeoutMillis;
                return this;
            }

            public LambdaFunctionSpecBuilder validateResponseSchema(boolean validateResponseSchema) {
                this.validateResponseSchema = validateResponseSchema;
                return this;
            }

            public LambdaFunctionSpecBuilder maxRetries(int maxRetries) {
                this.maxRetries = maxRetries;
                return this;
            }

            public LambdaFunctionSpecBuilder collectLogs(boolean collectLogs) {
                this.collectLogs = collectLogs;
                return this;
            }

            public LambdaFunctionSpec build() {
                LambdaFunctionSpec spec = new LambdaFunctionSpec();
                spec.functionName = this.functionName;
                spec.description = this.description;
                spec.defaultInvocationType = this.defaultInvocationType;
                spec.timeoutMillis = this.timeoutMillis;
                spec.validateResponseSchema = this.validateResponseSchema;
                spec.maxRetries = this.maxRetries;
                spec.collectLogs = this.collectLogs;
                return spec;
            }
        }
    }
} 