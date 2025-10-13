package com.sbpb.ddobak.server.config;

import com.sbpb.ddobak.server.config.security.JwtAuthenticationFilter;
import com.sbpb.ddobak.server.domain.auth.service.JwtService;
import com.sbpb.ddobak.server.domain.auth.service.TokenService;
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
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security 설정
 * JWT 기반 인증과 Apple 로그인을 지원하는 보안 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final TokenService tokenService;

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
            .formLogin(formLogin -> formLogin.disable())
            
            // JWT 인증 필터 추가
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, tokenService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정 - API Gateway + Lambda Proxy 통합에서 필요
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        List<String> allowedOrigins = new ArrayList<>(Arrays.asList(
            // 로컬 개발 환경
            "http://localhost:3000",
            "http://localhost:3001",
            // 배포된 프론트엔드
            "https://ddobak-frontend-webview.vercel.app"
        ));
        
        // .env 파일에서 API Gateway URL 추가 (보안을 위해 Gateway URL을 직접 넣으면 안됨!)
        // spring-dotenv 라이브러리를 통해 .env 파일의 환경 변수를 자동으로 로드함
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