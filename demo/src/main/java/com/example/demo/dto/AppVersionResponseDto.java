package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.enums.PlatformType;
import com.example.demo.enums.UpdateType;

public record AppVersionResponseDto(Long id,
    String version,
    PlatformType platform,
    LocalDateTime releaseDate,
    String changelog,
    UpdateType updateType,
    boolean active) {

}
