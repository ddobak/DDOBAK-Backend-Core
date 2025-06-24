package com.sbpb.ddobak.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AWS 관련 설정 속성을 관리하는 클래스
 * application.yml의 aws 섹션과 매핑됨
 */
@Data
@Component
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

     // AWS 리전 기본값
    private String region = "ap-northeast-2";

     // AWS 프로파일 기본값
    private String profile = "ddobak";

     // S3 관련 설정 기본값
    private S3Properties s3 = new S3Properties();
    
    // Step Functions 관련 설정
    private StepFunctionsProperties stepFunctions = new StepFunctionsProperties();

    @Data
    public static class S3Properties {
         // 테스트용 S3 버킷 기본값
        private String testBucket = "ddobak-test";
        
        // 서비스용 S3 버킷 (기본값은 test 버킷으로)
        private String serviceBucket = "ddobak-test";
    }
    
    @Data
    public static class StepFunctionsProperties {
        // 계약서 분석 State Machine ARN
        private String contractAnalysisStateMachineArn = "arn_for_step_functions";
    }
} 