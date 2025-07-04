package com.sbpb.ddobak.server.domain.auth.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Apple JWT 토큰 검증 및 파싱 유틸리티
 * Apple Identity Token의 서명 검증과 클레임 추출을 담당
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppleJwtUtils {
    
    @Value("${apple.oauth.issuer}")
    private String appleIssuer;
    
    @Value("${apple.oauth.client-id}")
    private String appleClientId;
    
    @Value("${apple.oauth.public-keys-url}")
    private String applePublicKeysUrl;
    
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    /**
     * Apple Identity Token을 검증하고 클레임을 추출
     * @param identityToken Apple에서 발급한 Identity Token
     * @return JWT 클레임
     * @throws Exception 토큰 검증 실패 시
     */
    public Claims parseAndValidateToken(String identityToken) throws Exception {
        // 1. JWT 헤더에서 kid(Key ID) 추출
        String keyId = extractKeyIdFromToken(identityToken);
        
        // 2. Apple 공개키 가져오기
        PublicKey publicKey = getApplePublicKey(keyId);
        
        // 3. JWT 토큰 검증 및 파싱
        Claims claims = Jwts.parser()
            .verifyWith(publicKey)
            .requireIssuer(appleIssuer)  // Apple 발급자 확인
            .requireAudience(appleClientId)  // 앱 클라이언트 ID 확인
            .build()
            .parseSignedClaims(identityToken)
            .getPayload();
        
        log.info("Apple Identity Token validated successfully for user: {}", claims.getSubject());
        return claims;
    }
    
    /**
     * JWT 헤더에서 Key ID(kid) 추출
     * @param token JWT 토큰
     * @return Key ID
     */
    private String extractKeyIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            String header = new String(Base64.getUrlDecoder().decode(parts[0]));
            Map<String, Object> headerMap = objectMapper.readValue(header, Map.class);
            return (String) headerMap.get("kid");
        } catch (Exception e) {
            log.error("Failed to extract key ID from token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token format", e);
        }
    }
    
    /**
     * Apple 공개키 서버에서 특정 Key ID의 공개키 가져오기
     * @param keyId Apple 공개키의 Key ID
     * @return RSA 공개키
     */
    private PublicKey getApplePublicKey(String keyId) throws Exception {
        // Apple 공개키 엔드포인트 호출
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(applePublicKeysUrl))
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch Apple public keys");
        }
        
        // 응답에서 해당 Key ID의 공개키 찾기
        Map<String, Object> keysResponse = objectMapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> keys = (List<Map<String, Object>>) keysResponse.get("keys");
        
        Map<String, Object> targetKey = keys.stream()
            .filter(key -> keyId.equals(key.get("kid")))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Apple public key not found for kid: " + keyId));
        
        // RSA 공개키 생성
        return createRSAPublicKey(
            (String) targetKey.get("n"),  // modulus
            (String) targetKey.get("e")   // exponent
        );
    }
    
    /**
     * RSA 공개키 생성
     * @param modulus Base64URL 인코딩된 modulus
     * @param exponent Base64URL 인코딩된 exponent
     * @return RSA 공개키
     */
    private PublicKey createRSAPublicKey(String modulus, String exponent) throws Exception {
        byte[] nBytes = Base64.getUrlDecoder().decode(modulus);
        byte[] eBytes = Base64.getUrlDecoder().decode(exponent);
        
        BigInteger n = new BigInteger(1, nBytes);
        BigInteger e = new BigInteger(1, eBytes);
        
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(n, e);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        
        return keyFactory.generatePublic(keySpec);
    }
} 