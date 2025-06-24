package com.sbpb.ddobak.server.infrastructure.aws.config;

import com.sbpb.ddobak.server.config.AwsProperties;
import com.sbpb.ddobak.server.infrastructure.aws.client.AwsClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sfn.SfnClient;

import java.util.HashMap;
import java.util.Map;

/**
 * AWS Infrastructure 설정 클래스
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AwsInfrastructureConfig {

    private final AwsClientFactory awsClientFactory;
    private final AwsProperties awsProperties;

    /**
     * AWS Lambda Client Bean 등록
     */
    @Bean
    public LambdaClient lambdaClient() {
        return awsClientFactory.createLambdaClient();
    }

    /**
     * AWS S3 Client Bean 등록
     */
    @Bean
    public S3Client s3Client() {
        return awsClientFactory.createS3Client();
    }

    /**
     * AWS Step Functions Client Bean 등록
     */
    @Bean
    public SfnClient stepFunctionsClient() {
        return awsClientFactory.createStepFunctionsClient();
    }

    /**
     * 운영용 Lambda 함수 설정 맵
     * 테스트용 설정은 별도 테스트 설정 클래스에서 관리
     */
    @Bean
    public Map<String, LambdaFunctionSpec> productionLambdaFunctions() {
        log.info("Initializing production Lambda function configurations");
        
        Map<String, LambdaFunctionSpec> functions = new HashMap<>();
        
        // OCR Lambda 설정 - 문서 OCR 처리용 비동기 함수
        functions.put("ocr_lambda", LambdaFunctionSpec.builder()
                .functionName("ocr_lambda")
                .description("문서 OCR 처리 Lambda 함수 (운영용)")
                .defaultInvocationType("Event") // 비동기 호출
                .timeoutMillis(30000L) // 30초 - OCR 처리 시간 고려
                .validateResponseSchema(true) // 응답 스키마 검증 활성화
                .maxRetries(2) // 재시도 2회 - OCR 처리 실패 시 재시도
                .collectLogs(true) // 로그 수집 활성화 - 운영 디버깅용
                .build());
        
        log.info("Production Lambda function configurations initialized: {}", functions.keySet());
        return functions;
    }

    /**
     * 운영용 Step Functions State Machine 설정 맵
     */
    @Bean
    public Map<String, StateMachineSpec> productionStateMachines() {
        log.info("Initializing production Step Functions state machine configurations");
        
        Map<String, StateMachineSpec> stateMachines = new HashMap<>();
        
        // 계약서 분석 State Machine 설정
        stateMachines.put("contract_analysis", StateMachineSpec.builder()
                .stateMachineArn(awsProperties.getStepFunctions().getContractAnalysisStateMachineArn())
                .name("contract-analysis-workflow")
                .description("계약서 분석 워크플로우 (OCR + AI 분석)")
                .timeoutMinutes(15) // 15분 타임아웃 - 전체 분석 프로세스 고려
                .validateInput(true) // 입력 스키마 검증 활성화
                .collectLogs(true) // 실행 로그 수집 활성화
                .build());
        
        log.info("Production state machine configurations initialized: {}", stateMachines.keySet());
        log.info("Contract analysis State Machine ARN: {}", awsProperties.getStepFunctions().getContractAnalysisStateMachineArn());
        return stateMachines;
    }

    /**
     * Lambda 함수 스펙 정의
     * 기존 LambdaFunctionConfig를 간소화한 버전
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

    /**
     * Step Functions State Machine 스펙 정의
     */
    public static class StateMachineSpec {
        private String stateMachineArn;
        private String name;
        private String description;
        private int timeoutMinutes;
        private boolean validateInput;
        private boolean collectLogs;

        // Builder 패턴
        public static StateMachineSpecBuilder builder() {
            return new StateMachineSpecBuilder();
        }

        // Getters
        public String getStateMachineArn() { return stateMachineArn; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getTimeoutMinutes() { return timeoutMinutes; }
        public boolean isValidateInput() { return validateInput; }
        public boolean isCollectLogs() { return collectLogs; }

        // Builder 클래스
        public static class StateMachineSpecBuilder {
            private String stateMachineArn;
            private String name;
            private String description;
            private int timeoutMinutes;
            private boolean validateInput;
            private boolean collectLogs;

            public StateMachineSpecBuilder stateMachineArn(String stateMachineArn) {
                this.stateMachineArn = stateMachineArn;
                return this;
            }

            public StateMachineSpecBuilder name(String name) {
                this.name = name;
                return this;
            }

            public StateMachineSpecBuilder description(String description) {
                this.description = description;
                return this;
            }

            public StateMachineSpecBuilder timeoutMinutes(int timeoutMinutes) {
                this.timeoutMinutes = timeoutMinutes;
                return this;
            }

            public StateMachineSpecBuilder validateInput(boolean validateInput) {
                this.validateInput = validateInput;
                return this;
            }

            public StateMachineSpecBuilder collectLogs(boolean collectLogs) {
                this.collectLogs = collectLogs;
                return this;
            }

            public StateMachineSpec build() {
                StateMachineSpec spec = new StateMachineSpec();
                spec.stateMachineArn = this.stateMachineArn;
                spec.name = this.name;
                spec.description = this.description;
                spec.timeoutMinutes = this.timeoutMinutes;
                spec.validateInput = this.validateInput;
                spec.collectLogs = this.collectLogs;
                return spec;
            }
        }
    }
} 