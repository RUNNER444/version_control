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

import com.example.demo.model.UserDevice;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserDeviceController {
    private List<UserDevice> userDevices = new ArrayList<>();

    @GetMapping("/userDevices")
    public List<UserDevice> getUserDevices() {
        return userDevices;
    }

    @GetMapping("/userDevices/{userId}")
    public ResponseEntity<UserDevice> getUserDevice(@PathVariable String userId) {
        for (UserDevice userDevice : userDevices) {
            if (userDevice.getUserId().equals(userId)) {
                return ResponseEntity.ok(userDevice);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/userDevices")
    public ResponseEntity<UserDevice> addUserDevice(@RequestBody @Valid UserDevice userDevice) {
        userDevice.setUserId("unknown");
        userDevices.add(userDevice);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDevice);
    }
}