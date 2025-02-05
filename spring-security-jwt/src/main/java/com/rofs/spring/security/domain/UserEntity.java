package com.rofs.spring.security.domain;

import com.rofs.spring.security.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Builder(builderMethodName = "of")
@Getter
public class UserEntity {
    private Long id;
    private String email;
    private String password;
    private String name;
    private Set<Role> roles;

}
