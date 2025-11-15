package com.example.demo.dto;

import com.example.demo.enums.PlatformType;

public record UserDeviceRequestDto(Long userId,
    PlatformType platform,
    String currentVersion) {

}
