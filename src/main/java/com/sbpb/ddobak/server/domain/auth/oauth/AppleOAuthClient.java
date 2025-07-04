package com.sbpb.ddobak.server.domain.auth.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Apple OAuth 클라이언트
 * Apple Identity Token 검증 및 사용자 정보 추출을 담당
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppleOAuthClient implements OAuthClient {
    
    private final AppleJwtUtils appleJwtUtils;
    private final ObjectMapper objectMapper;
    
    @Override
    public String getProviderName() {
        return "apple";
    }
    
    @Override
    public OAuthUserInfo getUserInfo(String identityToken) {
        try {
            // Apple Identity Token 검증 및 파싱
            Claims claims = appleJwtUtils.parseAndValidateToken(identityToken);
            
            // 클레임에서 사용자 정보 추출
            String providerId = claims.getSubject();  // Apple 사용자 고유 ID
            String email = claims.get("email", String.class);
            Boolean emailVerified = claims.get("email_verified", Boolean.class);
            
            // 사용자 이름은 별도로 전달되는 경우가 있음 (최초 로그인 시에만)
            String name = extractNameFromClaims(claims);
            
            return OAuthUserInfo.builder()
                .providerId(providerId)
                .email(email)
                .name(name)
                .provider(OAuthProvider.APPLE)
                .emailVerified(emailVerified != null ? emailVerified : false)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to parse Apple identity token: {}", e.getMessage());
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
} 