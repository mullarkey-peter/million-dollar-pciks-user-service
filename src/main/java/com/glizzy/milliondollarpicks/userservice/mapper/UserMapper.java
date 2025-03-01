package com.glizzy.milliondollarpicks.userservice.mapper;

import com.glizzy.milliondollarpicks.userservice.dto.UserDto;
import com.glizzy.milliondollarpicks.userservice.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between User entity and UserDto
 */
@Component
public class UserMapper {

    /**
     * Converts a User entity to a UserDto
     * @param user The User entity to convert
     * @return The converted UserDto
     */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .registrationDate(user.getRegistrationDate())
                .lastLoginDate(user.getLastLoginDate())
                .build();
    }
}