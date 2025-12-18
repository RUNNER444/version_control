package com.example.demo.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.demo.enums.NotificationType;
import com.example.demo.enums.UpdateType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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

    @Column(name = "device_id")
    private Long deviceId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private UserDevice device;

    @Column(name = "user_id", nullable = false)
    private Long userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @NotBlank
    @Column(name = "current_version")
    private String currentVersion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version", referencedColumnName = "version",
    insertable = false, updatable = false)
    private AppVersion currentVersionEntity;

    @Column(name = "latest_version")
    private String latestVersion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "latest_version", referencedColumnName = "version",
    insertable = false, updatable = false)
    private AppVersion latestVersionEntity;

    @Enumerated(EnumType.STRING)
    private UpdateType updateType;

    @Enumerated(EnumType.STRING)
    private NotificationType status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    private String message;
}
