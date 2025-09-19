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

import com.example.demo.model.Permission;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PermissionController {
    private List<Permission> permissions = new ArrayList<>();

    @GetMapping("/permissions")
    public List<Permission> getPermissions() {
        return permissions;
    }

    @GetMapping("/permissions/{id}")
    public ResponseEntity<Permission> getPermission(@PathVariable Long id) {
        for (Permission permission : permissions) {
            if (permission.getId().equals(id)) {
                return ResponseEntity.ok(permission);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/permissions")
    public ResponseEntity<Permission> addPermission(@RequestBody @Valid Permission permission) {
        permission.setId((long)permissions.size() + 1);
        permissions.add(permission);
        return ResponseEntity.status(HttpStatus.CREATED).body(permission);
    }
}