package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAll();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok().body(userService.getById(id));
    }
    

    @PostMapping("/users")
    public ResponseEntity<User> addUser(@RequestBody @Valid User user) {
        User newUser = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }
    
    @PutMapping("/users/{id}")
    public ResponseEntity <User> editUser(@PathVariable Long id, @RequestBody @Valid User user) {   
        User updated = userService.update(id, user);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping ("/users/{id}")
    public ResponseEntity <Void> deleteUser(@PathVariable Long id) {
        if (userService.deleteById(id)) {
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }
}