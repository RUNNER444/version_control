package com.example.demo.dto;

import com.example.demo.enums.PlatformType;
import com.example.demo.enums.UpdateType;

public record AppVersionRequestDto(String version,
    PlatformType platform,
    UpdateType updateType,
    boolean active) {

}
