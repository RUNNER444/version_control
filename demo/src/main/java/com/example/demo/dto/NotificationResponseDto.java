package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.enums.NotificationType;
import com.example.demo.enums.UpdateType;

public record NotificationResponseDto(
    Long id,
    Long deviceId,
    Long userId,
    String currentVersion,
    String latestVersion,
    UpdateType updateType,
    NotificationType status,
    LocalDateTime createdAt,
    LocalDateTime readAt,
    String message
) {

}
