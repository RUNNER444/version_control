package com.example.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.NotificationRequestDto;
import com.example.demo.dto.NotificationResponseDto;
import com.example.demo.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag (name = "Notifications", description = "Methods for sending and managing notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Operation (
    summary = "Get device's notifications",
    description = "Retrieves all notifications for a specific device by its ID")
    @GetMapping("/device/{deviceId}")
    public ResponseEntity <List <NotificationResponseDto>> getDeviceNotifications(
        @Parameter(description = "ID of the device to check notifications", required = true)
        @PathVariable Long deviceId) {
        logger.info("Received request to check notifications for device {}", deviceId);

        try {
            return ResponseEntity.ok(notificationService.getNotificationsByDeviceId(deviceId));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Id value is invalid: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while getting notifications for device {}. Error: {}", deviceId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation (
    summary = "Get user's notifications",
    description = "Retrieves all notifications for a specific user by his ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity <List <NotificationResponseDto>> getUserNotifications(
        @Parameter(description = "ID of the user to check notifications", required = true)
        @PathVariable Long userId) {
        logger.info("Received request to check notifications for user {}", userId);

        try {
            return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Id value is invalid: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while getting notifications for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation (
    summary = "Get user's unread notifications",
    description = "Retrieves all unread notifications for a specific user by his ID")
    @GetMapping("/user/unread/{userId}")
    public ResponseEntity <List <NotificationResponseDto>> getUserUnreadNotifications(
        @Parameter(description = "ID of the user to check unread notifications", required = true)
        @PathVariable Long userId) {
        logger.info("Received request to check unread notifications for user {}", userId);

        try {
            return ResponseEntity.ok(notificationService.getUnreadNotificationsByUserId(userId));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Id value is invalid: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while getting unread notifications for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation (
    summary = "Mark notification as read",
    description = "Changes the status of specified notification to READ")
    @PutMapping("/read/{notificationId}")
    public ResponseEntity <NotificationResponseDto> markAsRead (
        @Parameter(description = "ID of the notification to mark as read", required = true)
        @PathVariable Long notificationId) {
        logger.info("Received request to mark notification {} as read", notificationId);

        try {
            return ResponseEntity.ok(notificationService.markAsRead(notificationId));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Id value is invalid: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while marking notification {} as read. Error: {}", notificationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation (
    summary = "Mark notification as dismissed",
    description = "Changes the status of specified notification to DISMISSED")
    @PutMapping("/dismiss/{notificationId}")
    public ResponseEntity <NotificationResponseDto> markAsDismissed (
        @Parameter(description = "ID of the notification to mark as dismissed", required = true)
        @PathVariable Long notificationId) {
        logger.info("Received request to mark notification {} as dismissed", notificationId);

        try {
            return ResponseEntity.ok(notificationService.markAsDismissed(notificationId));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Id value is invalid: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while marking notification {} as dismissed. Error: {}", notificationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation (
    summary = "Create specified notification",
    description = "Creates a single notification from provided data")
    @PostMapping("/send/single")
    public ResponseEntity <NotificationResponseDto> createNotification (
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Notification data to create", required = true)
        @RequestBody @Valid NotificationRequestDto request) {
        logger.info("Received request to create single notification for user {}", request.userId());

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.createNotification(request));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Invalid data provided. Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
        catch (Exception e) {
            logger.error("Error while creating single notification. Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation (
    summary = "Delete old notification",
    description = "Deletes all notifications that are older than the given value of days")
    @PostMapping("/delete/{expirationDays}")
    public ResponseEntity <Void> deletOldNotifications (
        @Parameter (description = "Number of days that constraints the deleting process", required = true)
        @PathVariable Long expirationDays) {
        logger.info("Received request to delete all notifications older than {} days", expirationDays);

        try {
            notificationService.deleteOldNotifications(expirationDays);
            return ResponseEntity.noContent().build();
        }
        catch (IllegalArgumentException e) {
            logger.warn("Invalid value provided. Must be a number. Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
        catch (Exception e) {
            logger.error("Error while deleting old notifications. Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation (
    summary = "Send notifications to all outdated devices",
    description = "Sends notifications about a needing to update to all devices with MANDATORY or DEPRECATED app versions")
    @PostMapping("/send")
    public ResponseEntity <Void> sendToOutdated () {
        try {
            notificationService.sendNotificationsToOutdatedDevices();
            return ResponseEntity.ok().build();
        }
        catch (Exception e) {
            logger.error("Error while sending notifications to outdated devices. Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
