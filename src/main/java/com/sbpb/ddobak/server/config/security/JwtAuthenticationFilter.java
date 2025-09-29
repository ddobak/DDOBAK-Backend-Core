package com.sbpb.ddobak.server.config.security;

import com.sbpb.ddobak.server.domain.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
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

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
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
}
