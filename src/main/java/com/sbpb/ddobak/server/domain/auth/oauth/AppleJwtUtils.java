package com.sbpb.ddobak.server.domain.auth.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
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
    
    @Value("${apple.oauth.team-id}")
    private String appleTeamId;
    
    @Value("${apple.oauth.key-id}")
    private String appleKeyId;
    
    @Value("${apple.oauth.private-key}")
    private String applePrivateKey;
    
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    /**
     * Apple Identity Token을 검증하고 클레임을 추출
     * @param identityToken Apple에서 발급한 Identity Token
     * @return JWT 클레임
     * @throws Exception 토큰 검증 실패 시
     */
    public Claims parseAndValidateToken(String identityToken) throws Exception {
        log.debug("Starting Apple Identity Token validation");
        
        // 1. JWT 헤더에서 kid(Key ID) 추출
        String keyId = extractKeyIdFromToken(identityToken);
        log.debug("Extracted key ID from token: {}", keyId);
        
        // 2. Apple 공개키 가져오기
        PublicKey publicKey = getApplePublicKey(keyId);
        log.debug("Retrieved Apple public key for key ID: {}", keyId);
        
        // 3. JWT 토큰 검증 및 파싱
        log.debug("Validating token with issuer: {} and audience: {}", appleIssuer, appleClientId);
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
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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
    
    /**
     * Apple Client Secret JWT 생성
     * Token Revocation API 호출 시 필요한 client_secret 생성
     * @return Client Secret JWT
     * @throws Exception 키 파싱 또는 JWT 생성 실패 시
     */
    public String generateClientSecret() throws Exception {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(15777000); // 6개월
        
        // Private Key 파싱
        PrivateKey privateKey = parsePrivateKey(applePrivateKey);
        
        // JWT 생성
        return Jwts.builder()
            .header()
                .add("kid", appleKeyId)
                .add("alg", "ES256")
                .and()
            .issuer(appleTeamId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .audience().add("https://appleid.apple.com").and()
            .subject(appleClientId)
            .signWith(privateKey, Jwts.SIG.ES256)
            .compact();
    }
    
    /**
     * Apple Private Key
     * @param privateKeyContent Private Key 내용
     * @return PrivateKey 객체
     */
    private PrivateKey parsePrivateKey(String privateKeyContent) throws Exception {
        try {
            // PEM 형식의 헤더/푸터 제거
            String privateKeyPEM = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            
            // Base64 디코딩
            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
            
            // EC Private Key 생성
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("Failed to parse Apple private key: {}", e.getMessage());
            throw new RuntimeException("Invalid Apple private key", e);
        }
    }
} 