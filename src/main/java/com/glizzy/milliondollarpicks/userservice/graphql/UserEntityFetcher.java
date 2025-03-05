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
    public UserDto fetchUser(Map<String, Object> values) {
        if (values.containsKey("id")) {
            String id = (String) values.get("id");
            return userService.findUserById(Long.parseLong(id));
        } else if (values.containsKey("username")) {
            String username = (String) values.get("username");
            return userService.findUserByUsername(username);
        } else if (values.containsKey("email")) {
            String email = (String) values.get("email");
            return userService.findUserByEmail(email);
        }
        return null;
    }
}