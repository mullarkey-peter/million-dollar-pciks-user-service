package com.glizzy.milliondollarpicks.userservice.client;

import com.glizzy.milliondollarpicks.authservice.grpc.AuthServiceGrpc;
import com.glizzy.milliondollarpicks.authservice.grpc.TokenValidationRequest;
import com.glizzy.milliondollarpicks.authservice.grpc.TokenValidationResponse;
import com.glizzy.milliondollarpicks.authservice.grpc.UserInfoResponse;
import com.glizzy.milliondollarpicks.userservice.dto.TokenValidationResultDto;
import com.glizzy.milliondollarpicks.userservice.dto.UserInfoDto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "authentication.enabled", havingValue = "true", matchIfMissing = true)
public class AuthServiceClient {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceClient.class);

    @Value("${grpc.client.auth-service.host:auth-service}")
    private String authServiceHost;

    @Value("${grpc.client.auth-service.port:9090}")
    private int authServicePort;

    private ManagedChannel channel;
    private AuthServiceGrpc.AuthServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        log.info("Initializing gRPC client to auth-service at {}:{}", authServiceHost, authServicePort);
        channel = ManagedChannelBuilder.forAddress(authServiceHost, authServicePort)
                .usePlaintext()
                .build();
        blockingStub = AuthServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down gRPC client");
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Error shutting down gRPC channel: {}", e.getMessage());
            }
        }
    }

    public TokenValidationResultDto validateToken(String token) {
        log.debug("Calling auth-service to validate token");
        try {
            TokenValidationRequest request = TokenValidationRequest.newBuilder()
                    .setToken(token)
                    .build();
            TokenValidationResponse response = blockingStub.validateToken(request);
            log.debug("Token validation response: valid={}", response.getValid());
            return new TokenValidationResultDto(response.getValid(), response.getMessage());
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus(), e);
            return new TokenValidationResultDto(false, "RPC Error: " + e.getStatus());
        }
    }

    public UserInfoDto getUserInfo(String token) {
        log.debug("Calling auth-service to get user info from token");
        try {
            TokenValidationRequest request = TokenValidationRequest.newBuilder()
                    .setToken(token)
                    .build();
            UserInfoResponse response = blockingStub.getUserInfo(request);
            log.debug("User info response: success={}, userId={}", response.getSuccess(), response.getUserId());
            if (response.getSuccess()) {
                return new UserInfoDto(response.getUserId(), response.getUsername());
            } else {
                log.warn("Failed to get user info: {}", response.getMessage());
                return null;
            }
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus(), e);
            return null;
        }
    }
}