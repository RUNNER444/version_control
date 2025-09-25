package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private List <User> users = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        create(new User(null, "123", "123", true, null));
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public List <User> getAllByTitle (String title) {
        return userRepository.findAllByTitle(title);
    }

    public User create (User user) {
        return userRepository.save(user);
    }

    public User getById(Long id) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return userRepository.findById(id).orElse(null);
            }
        }
        return null;
    }

    public User update(Long id, User user) {
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setId(user.getId());
            existingUser.setUsername(user.getUsername());
            return userRepository.save(existingUser);
        }).orElse(null);
    }

    public boolean deleteById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        else {
            return false;
        }
    }
}
