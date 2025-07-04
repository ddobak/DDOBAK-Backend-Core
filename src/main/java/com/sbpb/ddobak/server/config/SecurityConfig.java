package com.sbpb.ddobak.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정
 * JWT 기반 인증과 Apple 로그인을 지원하는 보안 설정
 * CORS Preflight 요청 처리를 포함한 브라우저 호환성 확보
 */
//@Configuration
//@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용으로 불필요)
            .csrf(csrf -> csrf.disable())
            
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 세션 사용 안함 (JWT 기반 인증)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 경로별 인증 설정
            .authorizeHttpRequests(auth -> auth
                // 인증 API는 모든 접근 허용
                .requestMatchers("/api/auth/**").permitAll()
                
                // 개발용 엔드포인트 허용
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/ping").permitAll()

                // TODO: 완전한 인증 구현 전까지 모든 API 다 허용
                .requestMatchers("/api/**").permitAll()
                
                // Swagger UI 허용 (개발 환경)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // 개발/테스트용으로 모든 API 임시 허용 (운영 환경에서는 제거 필요)
                .requestMatchers("/api/**").permitAll()
                
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            
            // HTTP Basic 인증 비활성화
            .httpBasic(httpBasic -> httpBasic.disable())
            
            // Form 로그인 비활성화
            .formLogin(formLogin -> formLogin.disable())
            
            // JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 이전에 실행)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정
     * Preflight 요청 처리를 포함한 브라우저 호환성 확보
     * 개발 환경에서는 모든 도메인 허용, 운영 환경에서는 특정 도메인만 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 개발 환경에서는 모든 도메인 허용
        // 운영 환경에서는 실제 도메인으로 변경 필요
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 허용할 HTTP 메서드 (OPTIONS 포함 - Preflight 요청 지원)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        
        // 허용할 헤더 (Authorization 등 인증 헤더 포함)
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Refresh-Token"
        ));
        
        // 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);
        
        // 브라우저가 노출할 수 있는 헤더
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Refresh-Token"));
        
        // Preflight 요청 캐시 시간 (초 단위)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
} 