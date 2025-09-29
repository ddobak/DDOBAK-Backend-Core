package com.sbpb.ddobak.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

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
            
            // CORS 활성화 (API Gateway + Lambda Proxy에서 필요)
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
                
                // Swagger UI 허용 (개발 환경)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // 계약서 API와 사용자 API는 인증 필요 (개발/테스트용 permitAll설정 제거)
                
                // 꿀팁 아티클 API 공개 허용 (공개 API)
                .requestMatchers("/api/tips/**").permitAll()
                
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            
            // HTTP Basic 인증 비활성화
            .httpBasic(httpBasic -> httpBasic.disable())
            
            // Form 로그인 비활성화
            .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }

    /**
     * CORS 설정 - API Gateway + Lambda Proxy 통합에서 필요
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 특정 도메인 허용 (개발 및 테스트 환경)
        List<String> allowedOrigins = new ArrayList<>(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:3001",
            "https://ddobak-frontend-webview.vercel.app"
        ));
        
        // 환경 변수에서 API Gateway URL 추가 (보안을 위해 직접 코드에 URL을 하드코딩하지 않음)
        String apiGatewayUrl = System.getenv("API_GATEWAY_URL");
        if (apiGatewayUrl != null && !apiGatewayUrl.isEmpty()) {
            allowedOrigins.add(apiGatewayUrl);
        }
        
        configuration.setAllowedOrigins(allowedOrigins);
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));
        
        // 허용할 요청 헤더 (명시적 지정)
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type", 
            "Accept",
            "Origin",
            "X-Requested-With",
            "X-Request-Id",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // 브라우저가 읽을 수 있는 응답 헤더
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Refresh-Token", 
            "X-Request-Id",
            "Content-Disposition"
        ));
        
        // 인증 정보 포함 허용
        configuration.setAllowCredentials(true);
        
        // preflight 요청 캐시 시간
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
} 