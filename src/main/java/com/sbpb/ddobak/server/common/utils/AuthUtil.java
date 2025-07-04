package com.sbpb.ddobak.server.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 인증 관련 유틸리티
 * SecurityContext에서 사용자 정보를 추출하는 헬퍼 메서드 제공
 */
public class AuthUtil {

    /**
     * 현재 인증된 사용자 ID 반환
     * @return 사용자 ID (인증되지 않은 경우 null)
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getPrincipal().equals("anonymousUser")) {
            return authentication.getName();
        }
        
        return null;
    }

    /**
     * 현재 인증된 사용자 ID를 Long 타입으로 반환
     * @return 사용자 ID (인증되지 않은 경우 null)
     */
    public static Long getCurrentUserIdAsLong() {
        String userId = getCurrentUserId();
        return userId != null ? Long.parseLong(userId) : null;
    }

    /**
     * 현재 사용자가 인증되어 있는지 확인
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !authentication.getPrincipal().equals("anonymousUser");
    }

    /**
     * 인증이 필요한 경우 사용자 ID를 반환하고, 인증되지 않은 경우 예외 발생
     * @return 사용자 ID
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static String requireAuthentication() {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("Authentication required");
        }
        return userId;
    }

    /**
     * 인증이 필요한 경우 사용자 ID를 Long 타입으로 반환하고, 인증되지 않은 경우 예외 발생
     * @return 사용자 ID
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static Long requireAuthenticationAsLong() {
        return Long.parseLong(requireAuthentication());
    }
} 