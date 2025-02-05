package com.rofs.spring.security.jwt;

import com.rofs.spring.security.model.AuthUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider {
    public static final String ID_KEY = "id";
    public static final String NAME_KEY = "name";
    private final SecretKey key;

    private static final String AUTHORITIES_KEY = "authorities"; // 역할과 권한을 모두 포함

    record JwtPayload(Long id, String name, String subject, String authorities) {}

    public JwtProvider(JwtProperties jwtProperties) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }


    public String createToken(Authentication authentication, long expirationSeconds) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        var payload = new JwtPayload(
            authUser.getId(),
            authUser.getName(),
            authentication.getName(),
            authentication.getAuthorities().stream()
                          .map(GrantedAuthority::getAuthority)
                          .collect(Collectors.joining(","))
        );

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationSeconds * 1000);

        return Jwts.builder()
                   .subject(payload.subject())
                   .claim(ID_KEY, payload.id())
                   .claim(NAME_KEY, payload.name())
                   .claim(AUTHORITIES_KEY, payload.authorities())
                   .issuedAt(now)
                   .expiration(expiration)
                   .signWith(key)
                   .compact();
    }

    public Authentication getAuthentication(String token) {
        Jws<Claims> claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);

        Claims payload = claims.getPayload();

        Collection<? extends GrantedAuthority> authorities =
            (payload.get(AUTHORITIES_KEY, String.class) instanceof String roles) && !roles.isEmpty()
            ? Arrays.stream(roles.split(",")).map(SimpleGrantedAuthority::new).toList()
            : List.of();

        UserDetails principal = AuthUser.of()
                                        .username(payload.getSubject())
                                        .authorities(authorities)
                                        .password("")
                                        .id(payload.get(ID_KEY, Long.class))
                                        .name(payload.get(NAME_KEY, String.class))
                                        .build();

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}