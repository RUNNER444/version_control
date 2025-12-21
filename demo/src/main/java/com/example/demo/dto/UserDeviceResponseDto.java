package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.enums.PlatformType;

public record UserDeviceResponseDto(Long id,
    Long userId,
    PlatformType platform,
    String currentVersion,
    LocalDateTime lastSeen) {
    public Long getId() {return id;}
    public Long getUserId() {return userId;}
    public PlatformType getPlatform() {return platform;}
    public String getCurrentVersion() {return currentVersion;}
    public LocalDateTime getLastSeen() {return lastSeen;}
}
