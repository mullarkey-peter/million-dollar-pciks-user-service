package com.glizzy.milliondollarpicks.userservice.graphql;

import com.glizzy.milliondollarpicks.userservice.dto.UserDto;
import com.glizzy.milliondollarpicks.userservice.service.UserService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsEntityFetcher;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@DgsComponent
@RequiredArgsConstructor
public class UserEntityFetcher {

    private final UserService userService;

    @DgsEntityFetcher(name = "User")
    public UserDto fetchUserById(Map<String, Object> values) {
        String id = (String) values.get("id");
        return userService.findUserById(Long.parseLong(id));
    }
}