package com.example.demo.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserDevice {
    private String userId;
    private Platform platform;
    private String currentVersion;
    private LocalDateTime lastSeen;
}
