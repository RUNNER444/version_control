package com.example.demo.mapper;

import com.example.demo.dto.NotificationResponseDto;
import com.example.demo.model.Notification;

public class NotificationMapper {
    public static NotificationResponseDto notificationToNotificationResponseDto (Notification notification) {
        return new NotificationResponseDto(notification.getId(), notification.getDeviceId(), notification.getUserId(),
        notification.getCurrentVersion(), notification.getLatestVersion(),notification.getUpdateType(),
        notification.getStatus(), notification.getCreatedAt(), notification.getReadAt(), notification.getMessage());
    }
}
