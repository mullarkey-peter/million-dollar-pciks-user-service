package com.glizzy.milliondollarpicks.userservice.service;

import com.glizzy.milliondollarpicks.userservice.dto.UserDto;
import com.glizzy.milliondollarpicks.userservice.entity.User;
import com.glizzy.milliondollarpicks.userservice.mapper.UserMapper;
import com.glizzy.milliondollarpicks.userservice.exception.UserNotFoundException;
import com.glizzy.milliondollarpicks.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDto findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public UserDto updateLastLogin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        
        user.setLastLoginDate(OffsetDateTime.now());
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto createOrUpdateUser(String username) {
        return userRepository.findByUsername(username)
                .map(existingUser -> userMapper.toDto(userRepository.save(existingUser)))
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setRegistrationDate(OffsetDateTime.now());
                    return userMapper.toDto(userRepository.save(newUser));
                });
    }
}
