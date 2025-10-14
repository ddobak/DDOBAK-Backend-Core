package com.sbpb.ddobak.server.domain.auth.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Apple OAuth 클라이언트
 * Apple Identity Token 검증 및 사용자 정보 추출을 담당
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppleOAuthClient implements OAuthClient {
    
    @Getter
    private final AppleJwtUtils appleJwtUtils;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    @Value("${apple.oauth.client-id}")
    private String appleClientId;
    
    @Override
    public String getProviderName() {
        return "apple";
    }
    
    @Override
    public OAuthUserInfo getUserInfo(String identityToken) {
        try {
            log.debug("Starting Apple identity token parsing");
            
            // Apple Identity Token 검증 및 파싱
            Claims claims = appleJwtUtils.parseAndValidateToken(identityToken);
            
            // 클레임에서 사용자 정보 추출
            String providerId = claims.getSubject();  // Apple 사용자 고유 ID
            String email = claims.get("email", String.class);
            
            log.debug("Extracted user info - providerId: {}, email: {}", providerId, email);
            
            // 사용자 이름은 별도로 전달되는 경우가 있음 (최초 로그인 시에만)
            String name = extractNameFromClaims(claims);
            
            OAuthUserInfo userInfo = OAuthUserInfo.builder()
                .providerId(providerId)
                .email(email)
                .name(name)
                .provider(OAuthProvider.APPLE)
                .build();
                
            log.info("Successfully parsed Apple user info for: {}", email);
            return userInfo;
                
        } catch (Exception e) {
            log.error("Failed to parse Apple identity token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse Apple identity token", e);
        }
    }
    
    @Override
    public boolean supports(OAuthProvider provider) {
        return provider == OAuthProvider.APPLE;
    }
    
    /**
     * Apple JWT 클레임에서 사용자 이름 추출
     * Apple은 보통 이름 정보를 별도로 제공하지 않음
     * @param claims JWT 클레임
     * @return 사용자 이름 (없으면 null)
     */
    @SuppressWarnings("unchecked")
    private String extractNameFromClaims(Claims claims) {
        try {
            // Apple은 일반적으로 이름 정보를 JWT에 포함하지 않음
            // 별도의 user 파라미터로 전달받는 경우가 있음
            Object nameObj = claims.get("name");
            if (nameObj instanceof Map) {
                Map<String, Object> nameMap = (Map<String, Object>) nameObj;
                String firstName = (String) nameMap.get("firstName");
                String lastName = (String) nameMap.get("lastName");
                
                if (firstName != null && lastName != null) {
                    return firstName + " " + lastName;
                } else if (firstName != null) {
                    return firstName;
                } else if (lastName != null) {
                    return lastName;
                }
            }
            
            return null;  // 이름 정보가 없음
        } catch (Exception e) {
            log.warn("Failed to extract name from Apple claims: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Authorization Code를 사용하여 Apple Access Token 및 Refresh Token 발급
     * @param authorizationCode Apple에서 받은 Authorization Code
     * @return Apple Token Response (access_token, refresh_token 포함)
     * @throws Exception Token 발급 실패 시
     */
    @SuppressWarnings("unchecked")
    public AppleTokenResponse getTokens(String authorizationCode) throws Exception {
        try {
            String clientSecret = appleJwtUtils.generateClientSecret();
            
            // Request Body 생성 (application/x-www-form-urlencoded)
            String requestBody = "client_id=" + URLEncoder.encode(appleClientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&code=" + URLEncoder.encode(authorizationCode, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code";
            
            // HTTP Request 생성
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://appleid.apple.com/auth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            
            // HTTP Request 전송
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.error("Failed to get Apple tokens. Status: {}, Body: {}", response.statusCode(), response.body());
                throw new RuntimeException("Failed to get Apple tokens: " + response.body());
            }
            
            // Response 파싱
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
            
            return AppleTokenResponse.builder()
                .accessToken((String) responseMap.get("access_token"))
                .refreshToken((String) responseMap.get("refresh_token"))
                .expiresIn((Integer) responseMap.get("expires_in"))
                .tokenType((String) responseMap.get("token_type"))
                .build();
                
        } catch (Exception e) {
            log.error("Failed to get Apple tokens: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get Apple tokens", e);
        }
    }
    
    /**
     * Apple Token Revocation (계정 삭제 시 호출)
     * @param refreshToken 사용자의 Apple Refresh Token
     * @throws Exception Token Revocation 실패 시
     */
    public void revokeToken(String refreshToken) throws Exception {
        try {
            String clientSecret = appleJwtUtils.generateClientSecret();
            
            // Request Body 생성 (application/x-www-form-urlencoded)
            String requestBody = "client_id=" + URLEncoder.encode(appleClientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                + "&token_type_hint=refresh_token";
            
            // HTTP Request 생성
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://appleid.apple.com/auth/revoke"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            
            // HTTP Request 전송
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 200 또는 400 (이미 revoke된 경우)도 성공으로 처리
            if (response.statusCode() == 200 || response.statusCode() == 400) {
                log.info("Apple token revoked successfully");
            } else {
                log.error("Failed to revoke Apple token. Status: {}, Body: {}", response.statusCode(), response.body());
                throw new RuntimeException("Failed to revoke Apple token: " + response.body());
            }
            
        } catch (Exception e) {
            log.error("Failed to revoke Apple token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to revoke Apple token", e);
        }
    }
    
    /**
     * Apple Token Response DTO
     */
    @lombok.Builder
    @lombok.Getter
    public static class AppleTokenResponse {
        private String accessToken;
        private String refreshToken;
        private Integer expiresIn;
        private String tokenType;
    }
} 