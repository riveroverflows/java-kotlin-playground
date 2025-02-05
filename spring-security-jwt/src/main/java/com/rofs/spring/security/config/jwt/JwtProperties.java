package com.rofs.spring.security.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secretKey;

    private TokenExpiration token;

    @Getter
    @Setter
    public static class TokenExpiration {
        private long accessSeconds;      // 액세스 토큰 만료 시간(초)
        private long refreshSeconds;     // 리프레시 토큰 만료 시간(초)
    }
}