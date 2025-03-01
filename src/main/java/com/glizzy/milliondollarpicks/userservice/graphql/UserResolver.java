package com.glizzy.milliondollarpicks.userservice.graphql;

import com.glizzy.milliondollarpicks.userservice.dto.UserDto;
import com.glizzy.milliondollarpicks.userservice.service.UserService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GraphQL resolver for User-related operations
 */
@DgsComponent
@RequiredArgsConstructor
public class UserResolver {
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(UserResolver.class);

    /**
     * Query to fetch user by username
     */
    @DgsQuery
    public UserDto userByUsername(@InputArgument String username) {
        return userService.findUserByUsername(username);
    }

    /**
     * Query to fetch user by ID
     */
    @DgsQuery
    public UserDto userById(@InputArgument String id) {
        return userService.findUserById(Long.parseLong(id));
    }

    /**
     * Mutation to update user's last login date
     */
    @DgsMutation
    public UserDto updateLastLogin(@InputArgument String username) {
        return userService.updateLastLogin(username);
    }

    /**
     * Internal mutation for federation to create/update users
     */
    @DgsMutation
    public UserDto createOrUpdateUser(@InputArgument String username) {
        return userService.createOrUpdateUser(username);
    }
}