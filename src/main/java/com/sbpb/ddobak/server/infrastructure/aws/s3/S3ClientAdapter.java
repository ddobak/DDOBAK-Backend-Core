package com.sbpb.ddobak.server.infrastructure.aws.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AWS S3 클라이언트 어댑터
 * 
 * Infrastructure Layer에서 S3 연동을 담당하는 어댑터 클래스
 * 기존 S3Util을 Clean Architecture 패턴에 맞게 리팩토링
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class S3ClientAdapter {

    private final S3Client s3Client;

    /**
     * S3 버킷에 파일을 업로드합니다
     * 
     * @param bucketName S3 버킷 이름 (null 불가)
     * @param objectKey 저장될 객체의 키 (파일 경로)
     * @param inputStream 업로드할 데이터 스트림
     * @param contentLength 데이터 크기 (바이트)
     * @return 업로드 성공 시 true, 실패 시 false
     * @throws IllegalArgumentException bucketName 또는 objectKey가 null이거나 빈 문자열인 경우
     */
    public boolean uploadObject(String bucketName, String objectKey, InputStream inputStream, long contentLength) {
        validateBucketAndKey(bucketName, objectKey);
        validateInputStream(inputStream, contentLength);
        
        try {
            PutObjectRequest request = buildPutObjectRequest(bucketName, objectKey);
            RequestBody requestBody = RequestBody.fromInputStream(inputStream, contentLength);
            
            s3Client.putObject(request, requestBody);
            
            log.info("S3 upload successful: bucket={}, key={}, size={} bytes", 
                    bucketName, objectKey, contentLength);
            return true;
            
        } catch (Exception e) {
            log.error("S3 upload failed: bucket={}, key={}, size={} bytes", 
                    bucketName, objectKey, contentLength, e);
            return false;
        }
    }

    /**
     * S3 버킷에서 파일을 다운로드합니다
     * 
     * @param bucketName S3 버킷 이름
     * @param objectKey 다운로드할 객체의 키
     * @return 다운로드된 데이터 스트림, 실패 시 null
     */
    public InputStream getObject(String bucketName, String objectKey) {
        validateBucketAndKey(bucketName, objectKey);
        
        try {
            GetObjectRequest request = buildGetObjectRequest(bucketName, objectKey);
            InputStream inputStream = s3Client.getObject(request);
            
            log.info("S3 download successful: bucket={}, key={}", bucketName, objectKey);
            return inputStream;
            
        } catch (Exception e) {
            log.error("S3 download failed: bucket={}, key={}", bucketName, objectKey, e);
            return null;
        }
    }

    /**
     * S3 버킷에서 파일을 삭제합니다
     * 
     * @param bucketName S3 버킷 이름
     * @param objectKey 삭제할 객체의 키
     * @return 삭제 성공 시 true, 실패 시 false
     */
    public boolean deleteObject(String bucketName, String objectKey) {
        validateBucketAndKey(bucketName, objectKey);
        
        try {
            DeleteObjectRequest request = buildDeleteObjectRequest(bucketName, objectKey);
            s3Client.deleteObject(request);
            
            log.info("S3 delete successful: bucket={}, key={}", bucketName, objectKey);
            return true;
            
        } catch (Exception e) {
            log.error("S3 delete failed: bucket={}, key={}", bucketName, objectKey, e);
            return false;
        }
    }

    /**
     * S3 버킷의 객체 목록을 조회합니다
     * 
     * @param bucketName S3 버킷 이름
     * @param prefix 필터링할 접두사 (null인 경우 모든 객체 조회)
     * @return 객체 키 목록, 실패 시 빈 리스트
     */
    public List<String> listObjects(String bucketName, String prefix) {
        validateBucketName(bucketName);
        
        try {
            ListObjectsV2Request request = buildListObjectsRequest(bucketName, prefix);
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            
            List<String> objectKeys = extractObjectKeys(response);
            
            log.info("S3 list successful: bucket={}, prefix={}, count={}", 
                    bucketName, prefix, objectKeys.size());
            return objectKeys;
            
        } catch (Exception e) {
            log.error("S3 list failed: bucket={}, prefix={}", bucketName, prefix, e);
            return Collections.emptyList();
        }
    }

    /**
     * S3 객체의 존재 여부를 확인합니다
     * 
     * @param bucketName S3 버킷 이름
     * @param objectKey 확인할 객체의 키
     * @return 존재하면 true, 존재하지 않거나 오류 시 false
     */
    public boolean objectExists(String bucketName, String objectKey) {
        validateBucketAndKey(bucketName, objectKey);
        
        try {
            HeadObjectRequest request = buildHeadObjectRequest(bucketName, objectKey);
            s3Client.headObject(request);
            
            log.debug("S3 object exists: bucket={}, key={}", bucketName, objectKey);
            return true;
            
        } catch (NoSuchKeyException e) {
            log.debug("S3 object not found: bucket={}, key={}", bucketName, objectKey);
            return false;
        } catch (Exception e) {
            log.error("S3 existence check failed: bucket={}, key={}", bucketName, objectKey, e);
            return false;
        }
    }

    // ==================== Private Helper Methods ====================

    private void validateBucketAndKey(String bucketName, String objectKey) {
        validateBucketName(bucketName);
        validateObjectKey(objectKey);
    }

    private void validateBucketName(String bucketName) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
    }

    private void validateObjectKey(String objectKey) {
        if (objectKey == null || objectKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Object key cannot be null or empty");
        }
    }

    private void validateInputStream(InputStream inputStream, long contentLength) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        if (contentLength < 0) {
            throw new IllegalArgumentException("Content length cannot be negative");
        }
    }

    private PutObjectRequest buildPutObjectRequest(String bucketName, String objectKey) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
    }

    private GetObjectRequest buildGetObjectRequest(String bucketName, String objectKey) {
        return GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
    }

    private DeleteObjectRequest buildDeleteObjectRequest(String bucketName, String objectKey) {
        return DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
    }

    private ListObjectsV2Request buildListObjectsRequest(String bucketName, String prefix) {
        ListObjectsV2Request.Builder builder = ListObjectsV2Request.builder()
                .bucket(bucketName);
        
        if (prefix != null && !prefix.trim().isEmpty()) {
            builder.prefix(prefix);
        }
        
        return builder.build();
    }

    private HeadObjectRequest buildHeadObjectRequest(String bucketName, String objectKey) {
        return HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
    }

    private List<String> extractObjectKeys(ListObjectsV2Response response) {
        return response.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }
} 