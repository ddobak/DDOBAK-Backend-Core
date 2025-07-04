package com.sbpb.ddobak.server.config;

import com.sbpb.ddobak.server.domain.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 인증 필터
 * 
 * CORS Preflight 요청과 Authorization 헤더를 올바르게 처리하여
 * 브라우저 CORS 에러를 방지하는 JWT 인증 필터
 */
//@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Preflight 요청인지 확인 (OPTIONS 메서드)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.debug("Preflight request detected for: {}", request.getRequestURI());
            // Preflight 요청은 인증 없이 통과시킴
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Authorization 헤더에서 JWT 토큰 추출
        String jwt = resolveToken(request);

        // 3. JWT 토큰이 있고 유효한 경우 인증 설정
        if (jwt != null && jwtService.isTokenValid(jwt)) {
            try {
                // 토큰에서 사용자 정보 추출
                Long userId = jwtService.getUserIdFromToken(jwt);
                String email = jwtService.getEmailFromToken(jwt);

                // Spring Security 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userId.toString(), 
                        null, 
                        Collections.emptyList()
                    );

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("JWT authentication successful for user: {} ({})", email, userId);

            } catch (Exception e) {
                log.warn("JWT authentication failed: {}", e.getMessage());
                // 인증 실패 시 SecurityContext 클리어
                SecurityContextHolder.clearContext();
            }
        } else if (jwt != null) {
            log.warn("Invalid JWT token received");
            SecurityContextHolder.clearContext();
        }

        // 4. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     * Authorization 헤더에서 Bearer 토큰을 파싱
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }

    /**
     * 특정 요청에 대해 필터를 적용하지 않을지 결정
     * 인증이 필요 없는 경로는 여기서 제외 가능
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // 인증이 필요 없는 경로들
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/health") ||
               path.startsWith("/api/ping") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/h2-console/");
    }
} 