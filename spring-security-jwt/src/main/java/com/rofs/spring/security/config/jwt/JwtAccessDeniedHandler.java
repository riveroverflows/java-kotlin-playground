package com.rofs.spring.security.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class JwtAccessDeniedHandler extends AbstractSecurityExceptionHandler implements AccessDeniedHandler {

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        super.writeErrorResponse(request, response, HttpStatus.FORBIDDEN,
                                 "You do not have access to this resource");
    }

}
