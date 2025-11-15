package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.enums.PlatformType;

public record UserDeviceResponseDto(Long id,
    Long userId,
    PlatformType platform,
    String currentVersion,
    LocalDateTime lastSeen) {

}
