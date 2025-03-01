package com.glizzy.milliondollarpicks.userservice.filter;

import com.glizzy.milliondollarpicks.userservice.client.AuthServiceClient;
import com.glizzy.milliondollarpicks.userservice.dto.TokenValidationResultDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip authentication if disabled
        if (!authenticationEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip token validation for GraphQL playground
        String path = request.getRequestURI();
        if (path.contains("/graphiql")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            log.debug("Validating token from request");
            TokenValidationResultDto validationResult = authServiceClient.validateToken(token);

            if (validationResult.isValid()) {
                log.debug("Token is valid, proceeding with request");
                filterChain.doFilter(request, response);
            } else {
                log.warn("Invalid token: {}", validationResult.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: " + validationResult.getMessage());
            }
        } else {
            // If it's a GraphQL request that requires authentication, check the header
            if (path.equals("/graphql") && isAuthenticationRequired(request)) {
                log.warn("Missing Authorization header for GraphQL request");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Missing or invalid token");
            } else {
                // For other requests or GraphQL queries that don't need auth, proceed
                filterChain.doFilter(request, response);
            }
        }
    }

    // Helper method to determine if a GraphQL request requires authentication
    private boolean isAuthenticationRequired(HttpServletRequest request) {
        // Here you could inspect the GraphQL query to determine if it requires authentication
        // For simplicity, let's assume all mutations require authentication, but queries don't
        // In a real application, you'd need more sophisticated logic here
        try {
            String body = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            return body.contains("mutation");
        } catch (IOException e) {
            log.error("Error reading request body", e);
            return false;
        }
    }
}
