package com.example.demo.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDevice {
    private String userId;
    private Platform platform;
    private String currentVersion;
    private LocalDateTime lastSeen;
}
