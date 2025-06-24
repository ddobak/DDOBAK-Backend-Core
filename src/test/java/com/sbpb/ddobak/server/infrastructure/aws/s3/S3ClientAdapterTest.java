package com.sbpb.ddobak.server.infrastructure.aws.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * S3ClientAdapter 테스트 클래스
 * Infrastructure Layer의 AWS S3 어댑터 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("S3ClientAdapter 테스트")
class S3ClientAdapterTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3ClientAdapter s3ClientAdapter;

    private String testBucket;
    private String testKey;
    private InputStream testInputStream;
    private long testContentLength;

    @BeforeEach
    void setUp() {
        testBucket = "ddobak-test";
        testKey = "test/file.txt";
        testInputStream = new ByteArrayInputStream("test content".getBytes());
        testContentLength = "test content".getBytes().length;
    }

    @Test
    @DisplayName("파일 업로드 테스트 - 성공")
    void uploadObject_Success() {
        // Given
        PutObjectResponse mockResponse = PutObjectResponse.builder().build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(mockResponse);

        // When
        boolean result = s3ClientAdapter.uploadObject(testBucket, testKey, testInputStream, testContentLength);

        // Then
        assertTrue(result, "파일 업로드가 성공해야 합니다");
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("파일 업로드 테스트 - 실패")
    void uploadObject_Failure() {
        // Given
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("Upload failed"));

        // When
        boolean result = s3ClientAdapter.uploadObject(testBucket, testKey, testInputStream, testContentLength);

        // Then
        assertFalse(result, "예외 발생 시 업로드가 실패해야 합니다");
    }

    @Test
    @DisplayName("파일 다운로드 테스트 - 성공")
    void getObject_Success() {
        // Given
        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> mockResponseStream = mock(ResponseInputStream.class);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockResponseStream);

        // When
        InputStream result = s3ClientAdapter.getObject(testBucket, testKey);

        // Then
        assertNotNull(result, "다운로드된 InputStream이 null이 아니어야 합니다");
        assertEquals(mockResponseStream, result, "Mock에서 반환한 InputStream과 같아야 합니다");
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("파일 다운로드 테스트 - 실패")
    void getObject_Failure() {
        // Given
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(new RuntimeException("Download failed"));

        // When
        InputStream result = s3ClientAdapter.getObject(testBucket, testKey);

        // Then
        assertNull(result, "예외 발생 시 null을 반환해야 합니다");
    }

    @Test
    @DisplayName("파일 삭제 테스트 - 성공")
    void deleteObject_Success() {
        // Given
        DeleteObjectResponse mockResponse = DeleteObjectResponse.builder().build();
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(mockResponse);

        // When
        boolean result = s3ClientAdapter.deleteObject(testBucket, testKey);

        // Then
        assertTrue(result, "파일 삭제가 성공해야 합니다");
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("파일 삭제 테스트 - 실패")
    void deleteObject_Failure() {
        // Given
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(new RuntimeException("Delete failed"));

        // When
        boolean result = s3ClientAdapter.deleteObject(testBucket, testKey);

        // Then
        assertFalse(result, "예외 발생 시 삭제가 실패해야 합니다");
    }

    @Test
    @DisplayName("객체 목록 조회 테스트 - 성공")
    void listObjects_Success() {
        // Given
        S3Object object1 = S3Object.builder().key("file1.txt").build();
        S3Object object2 = S3Object.builder().key("file2.txt").build();
        
        ListObjectsV2Response mockResponse = ListObjectsV2Response.builder()
                .contents(object1, object2)
                .build();
        
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(mockResponse);

        // When
        List<String> result = s3ClientAdapter.listObjects(testBucket, "prefix");

        // Then
        assertNotNull(result, "결과 리스트가 null이 아니어야 합니다");
        assertEquals(2, result.size(), "2개의 객체가 반환되어야 합니다");
        assertTrue(result.contains("file1.txt"), "file1.txt가 포함되어야 합니다");
        assertTrue(result.contains("file2.txt"), "file2.txt가 포함되어야 합니다");
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    @DisplayName("객체 목록 조회 테스트 - 실패")
    void listObjects_Failure() {
        // Given
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(new RuntimeException("List failed"));

        // When
        List<String> result = s3ClientAdapter.listObjects(testBucket, "prefix");

        // Then
        assertNotNull(result, "예외 발생 시에도 빈 리스트를 반환해야 합니다");
        assertTrue(result.isEmpty(), "예외 발생 시 빈 리스트를 반환해야 합니다");
    }

    @Test
    @DisplayName("객체 존재 확인 테스트 - 존재함")
    void objectExists_True() {
        // Given
        HeadObjectResponse mockResponse = HeadObjectResponse.builder().build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(mockResponse);

        // When
        boolean result = s3ClientAdapter.objectExists(testBucket, testKey);

        // Then
        assertTrue(result, "객체가 존재할 때 true를 반환해야 합니다");
        verify(s3Client, times(1)).headObject(any(HeadObjectRequest.class));
    }

    @Test
    @DisplayName("객체 존재 확인 테스트 - 존재하지 않음")
    void objectExists_False() {
        // Given
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        // When
        boolean result = s3ClientAdapter.objectExists(testBucket, testKey);

        // Then
        assertFalse(result, "객체가 존재하지 않을 때 false를 반환해야 합니다");
    }

    @Test
    @DisplayName("객체 존재 확인 테스트 - 기타 예외")
    void objectExists_Exception() {
        // Given
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(new RuntimeException("Other error"));

        // When
        boolean result = s3ClientAdapter.objectExists(testBucket, testKey);

        // Then
        assertFalse(result, "기타 예외 발생 시 false를 반환해야 합니다");
    }

    @Test
    @DisplayName("순차적 테스트 파일 생성 - 통합 테스트")
    void createSequentialTestFile() {
        // Given - 실제 AWS S3 연결을 위해 Mock 비활성화
        s3ClientAdapter = new S3ClientAdapter(S3Client.builder()
                .region(software.amazon.awssdk.regions.Region.AP_NORTHEAST_2)
                .credentialsProvider(software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider.builder()
                        .profileName("ddobak")
                        .build())
                .build());
        
        String bucket = "ddobak-test";
        
        // When
        // 1. 현재 목록 조회
        System.out.println("=== S3ClientAdapter 통합 테스트 시작 ===");
        System.out.println("Target bucket: " + bucket);
        List<String> allObjects = s3ClientAdapter.listObjects(bucket, null);
        System.out.println("총 " + allObjects.size() + "개 객체 발견");
        allObjects.forEach(key -> System.out.println("  - " + key));
        
        // 2. test 파일 번호 찾기
        int maxNumber = findMaxTestFileNumber(allObjects);
        
        // 3. 다음 파일 생성
        int nextNumber = maxNumber + 1;
        String nextFile = "test" + nextNumber;
        System.out.println("생성할 파일: " + nextFile);
        
        String content = "Test file " + nextNumber + " created by S3ClientAdapter at " + java.time.LocalDateTime.now();
        java.io.InputStream inputStream = new java.io.ByteArrayInputStream(content.getBytes());
        
        boolean success = s3ClientAdapter.uploadObject(bucket, nextFile, inputStream, content.getBytes().length);
        
        // Then
        assertTrue(success, "파일 업로드가 성공해야 합니다");
        System.out.println("✅ S3ClientAdapter 파일 생성 완료: " + nextFile);
        System.out.println("=== S3ClientAdapter 통합 테스트 완료 ===");
    }

    /**
     * test 파일들 중 최대 번호를 찾습니다
     */
    private int findMaxTestFileNumber(List<String> objectKeys) {
        int maxNumber = 0;
        
        for (String key : objectKeys) {
            if (key.matches("^test\\d+$")) {
                try {
                    int number = Integer.parseInt(key.substring(4));
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    // 번호 파싱 실패는 무시
                }
            }
        }
        
        return maxNumber;
    }

    @Test
    @DisplayName("메서드 호출 순서 테스트")
    void methodCallSequence() {
        // Given
        String bucket = "test-bucket";
        String key = "test-file.txt";
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());

        // Mock 설정
        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> mockResponseStream = mock(ResponseInputStream.class);
        
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(
                ListObjectsV2Response.builder().contents(S3Object.builder().key(key).build()).build());
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockResponseStream);
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(DeleteObjectResponse.builder().build());

        // When & Then - 메서드 호출 순서대로 테스트
        
        // 1. 업로드
        boolean uploadResult = s3ClientAdapter.uploadObject(bucket, key, inputStream, 7);
        assertTrue(uploadResult, "업로드가 성공해야 합니다");

        // 2. 존재 확인
        boolean existsResult = s3ClientAdapter.objectExists(bucket, key);
        assertTrue(existsResult, "객체가 존재해야 합니다");

        // 3. 목록 조회
        List<String> listResult = s3ClientAdapter.listObjects(bucket, "test-");
        assertNotNull(listResult, "목록이 null이 아니어야 합니다");
        assertEquals(1, listResult.size(), "1개의 객체가 있어야 합니다");

        // 4. 다운로드
        InputStream downloadResult = s3ClientAdapter.getObject(bucket, key);
        assertNotNull(downloadResult, "다운로드 결과가 null이 아니어야 합니다");

        // 5. 삭제
        boolean deleteResult = s3ClientAdapter.deleteObject(bucket, key);
        assertTrue(deleteResult, "삭제가 성공해야 합니다");
    }
} 