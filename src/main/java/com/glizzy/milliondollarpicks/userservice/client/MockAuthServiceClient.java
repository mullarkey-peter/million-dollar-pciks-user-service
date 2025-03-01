package com.glizzy.milliondollarpicks.userservice.client;

import com.glizzy.milliondollarpicks.userservice.dto.TokenValidationResultDto;
import com.glizzy.milliondollarpicks.userservice.dto.UserInfoDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "authentication.enabled", havingValue = "false")
public class MockAuthServiceClient extends AuthServiceClient {
    @Override
    public TokenValidationResultDto validateToken(String token) {
        return new TokenValidationResultDto(true, "Authentication disabled");
    }

    @Override
    public UserInfoDto getUserInfo(String token) {
        return new UserInfoDto("test-user-id", "test-user");
    }

    @Override
    public void init() {
        // Do nothing for mock client
    }

    @Override
    public void shutdown() {
        // Do nothing for mock client
    }
}