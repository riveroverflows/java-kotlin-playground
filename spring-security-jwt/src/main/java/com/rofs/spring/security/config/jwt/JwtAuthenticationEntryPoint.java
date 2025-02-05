package com.rofs.spring.security.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class JwtAuthenticationEntryPoint extends AbstractSecurityExceptionHandler implements AuthenticationEntryPoint {

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        super.writeErrorResponse(request, response, HttpStatus.UNAUTHORIZED,
                                 "Invalid or missing JWT token");
    }

}
