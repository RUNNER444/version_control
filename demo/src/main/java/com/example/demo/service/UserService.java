package com.example.demo.service;

import java.util.List;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.dto.UserDto;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    public List<UserDto> getUsers() {
        List <UserDto> users = userRepository.findAll().stream().map(UserMapper::userToUserDto).toList();
        logger.info("Successfully retrieved {} users", users.size());
        return users;
    }

    public UserDto getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            logger.warn("User with ID {} not found", id);
            return new ResourceNotFoundException("User with id " + id + "not found");
        });

        logger.info("Successfully retrieved user with ID: {}", id);
        return UserMapper.userToUserDto(user);
    }

    public UserDto getUserDto(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> {
            logger.warn("User with username '{}' not found", username);
            return new ResourceNotFoundException("User with username " + username + "not found");
        });

        logger.info("Successfully retrieved UserDto for username: {}", username);
        return UserMapper.userToUserDto(user);
    }

    public User getUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> {
            logger.warn("User entity with username '{}' not found", username);
            return new ResourceNotFoundException("User with username " + username + "not found");
        });
        
        logger.info("Successfully retrieved User entity for username: {}", username);
        return user;
    }

    public void saveUser(User user) {
        userRepository.save(user);
        logger.info("Successfully saved user: {}", user.getUsername());
    }
}
