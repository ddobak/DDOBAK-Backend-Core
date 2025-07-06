package com.sbpb.ddobak.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 * JWT 기반 인증과 Apple 로그인을 지원하는 보안 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용으로 불필요)
            .csrf(csrf -> csrf.disable())
            
            // CORS 비활성화 (API Gateway에서 처리)
            .cors(cors -> cors.disable())
            
            // 세션 사용 안함 (JWT 기반 인증)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 경로별 인증 설정
            .authorizeHttpRequests(auth -> auth
                // 인증 API는 모든 접근 허용
                .requestMatchers("/api/auth/**").permitAll()
                
                // 개발용 엔드포인트 허용
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/ping").permitAll()
                
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
            .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }


} 