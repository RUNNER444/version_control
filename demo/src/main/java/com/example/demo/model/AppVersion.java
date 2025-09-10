package com.example.demo.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AppVersion {
    private String version;
    private Platform platform;
    private LocalDateTime releaseDate;
    private String changelog;
    private UpdateType updateType;
    private boolean isActive;
}
