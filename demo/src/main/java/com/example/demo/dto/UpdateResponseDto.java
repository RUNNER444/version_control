package com.example.demo.dto;

import com.example.demo.enums.UpdateType;

public record UpdateResponseDto(Long userDeviceId,
    boolean updateAvailable,
    String currentVersion,
    String latestVersion,
    UpdateType updateType
) {

}
