package com.rofs.spring.security.config.domain;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {
    private Map<String, UserEntity> emailToUser;

    public UserEntity save(UserEntity user) {
        emailToUser.put(user.getEmail(), user);
        return user;
    }

    public Optional<UserEntity> findByEmail(String email) {
        return Optional.ofNullable(emailToUser.get(email));
    }
}
