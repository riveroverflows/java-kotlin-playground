package com.rofs.spring.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rofs.spring.security.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public abstract class AbstractSecurityExceptionHandler {

    private final ObjectMapper objectMapper;

    protected void writeErrorResponse(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message) throws IOException {
        int statusCode = status.value();
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        PrintWriter writer = response.getWriter();
        writer.write(
            objectMapper.writeValueAsString(
                createErrorResponse(request, statusCode, status, message)
            ));
        writer.flush();
    }

    private static ErrorResponse createErrorResponse(HttpServletRequest request,
                                                     int statusCode,
                                                     HttpStatus status,
                                                     String message) {
        return ErrorResponse.of()
                            .status(statusCode)
                            .error(status.getReasonPhrase())
                            .message(message)
                            .path(request.getRequestURI())
                            .build();
    }
}