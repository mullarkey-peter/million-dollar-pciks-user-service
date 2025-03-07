package com.glizzy.milliondollarpicks.userservice.filter;

import com.glizzy.milliondollarpicks.userservice.client.AuthServiceClient;
import com.glizzy.milliondollarpicks.userservice.dto.TokenValidationResultDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import java.io.IOException;
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

        // Always allow GraphiQL requests
        if (path.contains("/graphiql")) {
            filterChain.doFilter(requestWrapper, response);
            return;
        }

        // Check if it's a GraphQL request
        if (path.equals("/graphql")) {
            String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

            boolean isIntrospection = requestBody.contains("__schema") ||
                    requestBody.contains("IntrospectionQuery") ||
                    requestBody.contains("getIntrospectionQuery");

            if (isIntrospection) {
                log.debug("Introspection query detected, allowing");
                filterChain.doFilter(requestWrapper, response);
                return;
            }

            // Check if it's a federated service discovery query
            if (requestBody.contains("_service") && requestBody.contains("sdl")) {
                log.debug("Federation service discovery query detected, allowing");
                filterChain.doFilter(requestWrapper, response);
                return;
            }

            // Check if it's the createOrUpdateUser mutation
            if(isCreateOrUpdateUserMutation(requestBody)) {
                log.debug("createOrUpdateUser mutation detected, allowing");
                return;
            }

            // For all other GraphQL requests, require authentication
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
                log.warn("Missing Authorization header for GraphQL request");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Missing or invalid token");
            }
        } else {
            // For non-GraphQL paths, just proceed
            filterChain.doFilter(requestWrapper, response);
        }
    }

    private boolean isCreateOrUpdateUserMutation(String requestBody) {
        return requestBody.contains("mutation") &&
                requestBody.contains("createOrUpdateUser") &&
                !requestBody.contains("updateLastLogin");
    }
}