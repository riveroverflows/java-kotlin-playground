package com.rofs.spring.security.service;

import com.rofs.spring.security.config.security.AuthUser;
import com.rofs.spring.security.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                             .map(user -> AuthUser.of()
                                                  .username(user.getEmail())
                                                  .password(user.getPassword())
                                                  .authorities(user.getRoles().stream()
                                                                   .map(role -> new SimpleGrantedAuthority(role.name()))
                                                                   .collect(Collectors.toSet()))
                                                  .id(user.getId())
                                                  .name(user.getName())
                                                  .build())
                             .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
