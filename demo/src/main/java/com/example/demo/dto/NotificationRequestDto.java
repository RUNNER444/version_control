package com.example.demo.dto;

import com.example.demo.enums.UpdateType;

public record NotificationRequestDto(
    Long deviceId,
    Long userId,
    String currentVersion,
    String latestVersion,
    UpdateType updateType,
    String message
) {

}
