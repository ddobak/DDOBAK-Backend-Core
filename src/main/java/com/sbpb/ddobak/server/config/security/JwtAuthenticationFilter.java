package com.sbpb.ddobak.server.config.security;

import com.sbpb.ddobak.server.domain.auth.service.JwtService;
import com.sbpb.ddobak.server.domain.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

/**
 * JWT 토큰을 처리하는 인증 필터
 * 
 * 요청 헤더에서 JWT 토큰을 추출+검증 -> SecurityContext에 인증 정보를 설정함.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenService tokenService;

    public JwtAuthenticationFilter(JwtService jwtService, TokenService tokenService) {
        this.jwtService = jwtService;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            final String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            
            final String jwt = authHeader.substring(7);
            
            if (jwtService.isTokenValid(jwt)) {
                Long userId = jwtService.getUserIdFromToken(jwt);
                
                // AccessToken 무효화 검증 (리프레시 토큰 갱신 후 "무조건"!! 기존 AccessToken 무효화)
                if (!isAccessTokenStillValid(userId, jwt)) {
                    log.debug("AccessToken이 무효화되었습니다 - 사용자 ID: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }
                
                String email = null;
                
                try {
                    email = jwtService.getEmailFromToken(jwt);
                } catch (Exception e) {
                    log.debug("JWT 토큰에서 이메일을 추출할 수 없습니다: {}", e.getMessage());
                }
                
                log.debug("JWT 토큰 인증 성공 - 사용자 ID: {}, 이메일: {}", userId, email);
                
                // 인증 객체 생성 및 SecurityContext에 설정
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userId, null, Collections.emptyList()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                log.debug("유효하지 않은 JWT 토큰입니다.");
            }
        } catch (Exception e) {
            log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * AccessToken이 여전히 유효한지 확인
     * 리프레시 토큰 갱신 후 기존 AccessToken이 무효화되었는지 검증
     * 
     * @param userId 사용자 ID
     * @param accessToken AccessToken
     * @return AccessToken 유효 여부
     */
    private boolean isAccessTokenStillValid(Long userId, String accessToken) {
        try {
            // AccessToken의 발급 시간(iat) 조회
            Instant tokenIssuedAt = jwtService.getTokenIssuedAt(accessToken);
            
            // DB에서 AccessToken 무효화 기준 시간 조회
            Instant accessTokenValidAfter = tokenService.getAccessTokenValidAfter(userId);
            
            // 토큰 발급 시간이 무효화 기준 시간보다 이후여야 유효
            boolean isValid = tokenIssuedAt.isAfter(accessTokenValidAfter);
            
            if (!isValid) {
                log.debug("AccessToken 무효화됨 - UserId: {}, TokenIssuedAt: {}, ValidAfter: {}", 
                    userId, tokenIssuedAt, accessTokenValidAfter);
            }
            
            return isValid;
        } catch (Exception e) {
            log.warn("AccessToken 유효성 검증 중 오류: {}", e.getMessage());
            return false; // 오류 발생 시 안전하게 무효로 처리
        }
    }
}
