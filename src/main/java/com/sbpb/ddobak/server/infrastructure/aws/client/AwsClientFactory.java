package com.sbpb.ddobak.server.infrastructure.aws.client;

import com.sbpb.ddobak.server.config.AwsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sfn.SfnClient;

/**
 * AWS 클라이언트 생성을 담당하는 팩토리 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwsClientFactory {

    private final AwsProperties awsProperties;

    /**
     * AWS Lambda 클라이언트 생성
     * 
     * @return 설정된 Lambda 클라이언트
     */
    public LambdaClient createLambdaClient() {
        log.info("Creating AWS Lambda Client with profile: {} and region: {}", 
                awsProperties.getProfile(), awsProperties.getRegion());
        
        return LambdaClient.builder()
                .region(getConfiguredRegion())
                .credentialsProvider(createCredentialsProvider())
                .build();
    }

    /**
     * AWS S3 클라이언트 생성
     * 
     * @return 설정된 S3 클라이언트
     */
    public S3Client createS3Client() {
        log.info("Creating AWS S3 Client with profile: {} and region: {}", 
                awsProperties.getProfile(), awsProperties.getRegion());
        
        return S3Client.builder()
                .region(getConfiguredRegion())
                .credentialsProvider(createCredentialsProvider())
                .build();
    }

    /**
     * AWS Step Functions 클라이언트 생성
     * 
     * @return 설정된 Step Functions 클라이언트
     */
    public SfnClient createStepFunctionsClient() {
        log.info("Creating AWS Step Functions Client with profile: {} and region: {}", 
                awsProperties.getProfile(), awsProperties.getRegion());
        
        return SfnClient.builder()
                .region(getConfiguredRegion())
                .credentialsProvider(createCredentialsProvider())
                .build();
    }

    /**
     * 공통 인증 프로바이더 생성
     * 
     * @return 설정된 인증 프로바이더
     */
    private ProfileCredentialsProvider createCredentialsProvider() {
        return ProfileCredentialsProvider.builder()
                .profileName(awsProperties.getProfile())
                .build();
    }

    /**
     * 공통 리전 설정
     * 
     * @return 설정된 리전
     */
    private Region getConfiguredRegion() {
        return Region.of(awsProperties.getRegion());
    }
} 