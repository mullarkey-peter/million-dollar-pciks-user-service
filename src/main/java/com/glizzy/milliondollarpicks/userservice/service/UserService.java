package com.glizzy.milliondollarpicks.userservice.service;

import com.glizzy.milliondollarpicks.userservice.dto.UserDto;

public interface UserService {
    UserDto findUserByUsername(String username);
    UserDto findUserById(Long id);
    UserDto findUserByEmail(String email);
    UserDto updateLastLogin(String username);
    UserDto createOrUpdateUser(String username, String email); // For federation to create/update users
}