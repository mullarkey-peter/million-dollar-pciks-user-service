package com.glizzy.milliondollarpicks.userservice.filter;

import com.glizzy.milliondollarpicks.userservice.client.AuthServiceClient;
import com.glizzy.milliondollarpicks.userservice.dto.TokenValidationResultDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ReadListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final AuthServiceClient authServiceClient;

    @Value("${authentication.enabled:true}")
    private boolean authenticationEnabled;

    public JwtAuthenticationFilter(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);

        if (!authenticationEnabled) {
            filterChain.doFilter(requestWrapper, response);
            return;
        }

        String path = requestWrapper.getRequestURI();
        if (path.contains("/graphiql")) {
            filterChain.doFilter(requestWrapper, response);
            return;
        }

        String authHeader = requestWrapper.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("Validating token from request");
            TokenValidationResultDto validationResult = authServiceClient.validateToken(token);
            if (validationResult.isValid()) {
                log.debug("Token is valid, proceeding with request");
                filterChain.doFilter(requestWrapper, response);
            } else {
                log.warn("Invalid token: {}", validationResult.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: " + validationResult.getMessage());
            }
        } else {
            if (path.equals("/graphql") && isAuthenticationRequired(requestWrapper)) {
                log.warn("Missing Authorization header for GraphQL request");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Missing or invalid token");
            } else {
                filterChain.doFilter(requestWrapper, response);
            }
        }
    }

    private boolean isAuthenticationRequired(ContentCachingRequestWrapper request) {
        try {
            // Get the content as a String - this doesn't consume the input stream
            String body = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
            return body.contains("mutation");
        } catch (Exception e) {
            log.error("Error reading request body", e);
            return false;
        }
    }
}