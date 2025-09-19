package com.example.demo.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Role;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class RoleController {
    private List<Role> roles = new ArrayList<>();

    @GetMapping("/roles")
    public List<Role> getRoles() {
        return roles;
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<Role> getRole(@PathVariable Long id) {
        for (Role role : roles) {
            if (role.getId().equals(id)) {
                return ResponseEntity.ok(role);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/roles")
    public ResponseEntity<Role> addRole(@RequestBody @Valid Role role) {
        role.setId((long)roles.size() + 1);
        roles.add(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }
}