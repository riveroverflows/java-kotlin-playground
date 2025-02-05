package com.rofs.spring.security.config.security;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;


@Getter
public class AuthUser extends User {
    private final Long id;
    private final String name;

    @Builder(builderMethodName = "of")
    private AuthUser(Long id, String name,
                     String username, String password,
                     Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.name = name;
    }
}