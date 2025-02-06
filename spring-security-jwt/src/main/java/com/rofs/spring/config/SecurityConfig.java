package com.rofs.spring.config;

import com.rofs.spring.security.filter.JwtAuthenticationFilter;
import com.rofs.spring.security.jwt.JwtAccessDeniedHandler;
import com.rofs.spring.security.jwt.JwtAuthenticationEntryPoint;
import com.rofs.spring.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)

            // 인증 및 권한 예외 처리 설정
            .exceptionHandling(customizer ->
                                   customizer.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                             .accessDeniedHandler(jwtAccessDeniedHandler)
            )

            // 세션을 사용하지 않는 Stateless 설정
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 권한별 접근 설정
            .authorizeHttpRequests(auth -> auth
                // H2 Console 인증 없이 허용
                .requestMatchers("/h2-console/**").permitAll()

                // 인증 없이 접근 가능한 URL 설정
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/refresh-token").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()

                // 특정 권한이 필요한 요청
                .requestMatchers(HttpMethod.GET, "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users/**").hasAnyRole("ADMIN", "MODERATOR")

                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )

            // H2 Console의 iframe 사용을 허용하기 위해 `frameOptions().sameOrigin()` 사용
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

            // JWT 필터 추가 (Spring Security 인증 전에 실행)
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 도메인을 명시적으로 지정
        // 운영 환경에선 * 사용하지 않는 것이 좋음
        configuration.setAllowedOrigins(List.of(
            "http://localhost:8080"
        ));

        // 필요한 HTTP 메서드만 허용
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE"
        ));

        // 필수 헤더만 허용
        configuration.setAllowedHeaders(List.of(
            "Authorization", // 인증 토큰/자격 증명
            "Content-Type",
            "X-Requested-With",  // ajax 요청 식별
            "Accept",  // 클라이언트가 받아들일 수 있는 컨텐츠 타입: 응답 타입
            "Origin"  // 요청이 어디서 왔는지: 요청 출처 도메인
        ));

        // 노출할 응답 헤더 제한
        configuration.setExposedHeaders(List.of(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));

        configuration.setMaxAge(3600L); // preflight 캐시 시간
        configuration.setAllowCredentials(true); // jwt 인증 시 필요

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider);
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
