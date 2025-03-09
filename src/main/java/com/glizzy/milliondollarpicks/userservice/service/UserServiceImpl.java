package com.glizzy.milliondollarpicks.userservice.service;

import com.glizzy.milliondollarpicks.userservice.client.AuthServiceClient;
import com.glizzy.milliondollarpicks.userservice.dto.TokenValidationResultDto;
import com.glizzy.milliondollarpicks.userservice.dto.UserDto;
import com.glizzy.milliondollarpicks.userservice.dto.UserInfoDto;
import com.glizzy.milliondollarpicks.userservice.entity.User;
import com.glizzy.milliondollarpicks.userservice.exception.AuthenticationException;
import com.glizzy.milliondollarpicks.userservice.exception.UserNotFoundException;
import com.glizzy.milliondollarpicks.userservice.mapper.UserMapper;
import com.glizzy.milliondollarpicks.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthServiceClient authServiceClient;

    @Value("${authentication.enabled:true}")
    private boolean authenticationEnabled;

    @Value("${authentication.bypass-header:X-Apollo-Gateway}")
    private String bypassHeader;

    /**
     * Validates user authentication from the current request context
     * @param requiredUsername If provided, also validates that the authenticated user matches this username
     * @return The authenticated user's info, or null if authentication is disabled/bypassed
     * @throws AuthenticationException if authentication fails
     */
    private UserInfoDto validateAuthentication(String requiredUsername) {
        if (!authenticationEnabled) {
            log.debug("Authentication disabled by configuration");
            return null;
        }

        // Get the current request from the context
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.warn("No request attributes found in context, cannot authenticate");
            return null;
        }

        HttpServletRequest request = requestAttributes.getRequest();

        // Check for bypass header (from gateway)
        if (request.getHeader(bypassHeader) != null) {
            log.debug("Authentication bypassed due to header: {}", bypassHeader);
            return null;
        }

        // Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationException("Authentication required");
        }

        String token = authHeader.substring(7);

        // Validate token with auth service
        TokenValidationResultDto validationResult = authServiceClient.validateToken(token);
        if (!validationResult.isValid()) {
            throw new AuthenticationException("Invalid token: " + validationResult.getMessage());
        }

        // Get user info from token
        UserInfoDto userInfo = authServiceClient.getUserInfo(token);
        if (userInfo == null) {
            throw new AuthenticationException("Failed to get user info from token");
        }

        // If a specific username is required, validate it
        if (requiredUsername != null && !requiredUsername.equals(userInfo.getUsername())) {
            throw new AuthenticationException("You are not authorized to access this user's data");
        }

        return userInfo;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findUserByUsername(String username) {
        // Authenticate user - only allow users to access their own data
        validateAuthentication(username);

        return userRepository.findByUsername(username)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findUserById(Long id) {
        // For ID-based lookup, first get the user to check username
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        // Then authenticate
        validateAuthentication(user.getUsername());

        return userMapper.toDto(user);
    }

    @Override
    public UserDto findUserByEmail(String email) {
        // For email-based lookup, similar to ID
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        validateAuthentication(user.getUsername());

        return userMapper.toDto(user);
    }

    @Override
    public UserDto updateLastLogin(String username) {
        // Authenticate - only allow users to update their own login time
        validateAuthentication(username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        user.setLastLoginDate(OffsetDateTime.now());
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto createOrUpdateUser(String username, String email) {
        // This operation is special - might be called during signup
        // For updates, authenticate that user is updating their own record
        UserInfoDto userInfo = null;
        try {
            userInfo = validateAuthentication(username);
        } catch (AuthenticationException e) {
            // For this method, we'll allow unauthenticated calls if:
            // 1. It's a new user (not an update)
            if (!userRepository.existsByUsername(username)) {
                log.debug("Creating new user {} without authentication", username);
            } else {
                // It's an existing user but no valid auth
                throw e;
            }
        }

        return userRepository.findByUsername(username)
                .map(existingUser -> {
                    // This is an update - already authenticated above
                    if (email != null) {
                        existingUser.setEmail(email);
                    }
                    return userMapper.toDto(userRepository.save(existingUser));
                })
                .orElseGet(() -> {
                    // This is a new user creation
                    User newUser = new User();
                    newUser.setUsername(username);
                    if (email != null) {
                        newUser.setEmail(email);
                    } else {
                        newUser.setEmail(username + "@example.com");
                    }
                    newUser.setRegistrationDate(OffsetDateTime.now());
                    return userMapper.toDto(userRepository.save(newUser));
                });
    }
}