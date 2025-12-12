package com.example.demo.model;

import java.time.LocalDateTime;

import com.example.demo.enums.NotificationType;
import com.example.demo.enums.UpdateType;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table (indexes = {
    @Index(name = "idx_device_id", columnList = "deviceId"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_status", columnList = "status")})
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long deviceId;

    private Long userId;

    private String currentVersion;

    private String latestVersion;

    private UpdateType updateType;

    private NotificationType status;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    private String message;
}
