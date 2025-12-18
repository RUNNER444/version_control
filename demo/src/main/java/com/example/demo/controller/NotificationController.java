package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @Operation (
    summary = "Get device's notifications",
    description = "Retrieves all notifications for a specific device by its ID")
    @GetMapping("/device/{deviceId}")
    public ResponseEntity <List <NotificationResponseDto>> getDeviceNotifications(
    @Parameter(description = "ID of the device to check notifications", required = true)
    @PathVariable Long deviceId) {
        return ResponseEntity.ok(notificationService.getNotificationsByDeviceId(deviceId));
    }

    @Operation (
    summary = "Get user's notifications",
    description = "Retrieves all notifications for a specific user by his ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity <List <NotificationResponseDto>> getUserNotifications(
    @Parameter(description = "ID of the user to check notifications", required = true)
    @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }

    @Operation (
    summary = "Get user's unread notifications",
    description = "Retrieves all unread notifications for a specific user by his ID")
    @GetMapping("/user/unread/{userId}")
    public ResponseEntity <List <NotificationResponseDto>> getUserUnreadNotifications(
    @Parameter(description = "ID of the user to check unread notifications", required = true)
    @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationsByUserId(userId));
    }

    @Operation (
    summary = "Mark notification as read",
    description = "Changes the status of specified notification to READ")
    @PutMapping("/read/{notificationId}")
    public ResponseEntity <NotificationResponseDto> markAsRead (
    @Parameter(description = "ID of the notification to mark as read", required = true)
    @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @Operation (
    summary = "Mark notification as dismissed",
    description = "Changes the status of specified notification to DISMISSED")
    @PutMapping("/dismiss/{notificationId}")
    public ResponseEntity <NotificationResponseDto> markAsDismissed (
    @Parameter(description = "ID of the notification to mark as dismissed", required = true)
    @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsDismissed(notificationId));
    }

    @Operation (
    summary = "Create specified notification",
    description = "Creates a single notification from provided data")
    @PostMapping("/send/single")
    public ResponseEntity <NotificationResponseDto> createNotification (
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Notification data to create", required = true)
    @RequestBody @Valid NotificationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.createNotification(request));
    }

    @Operation (
    summary = "Delete old notifications",
    description = "Deletes all notifications that are older than the given value of days")
    @DeleteMapping("/delete/{expirationDays}")
    public ResponseEntity <Void> deletOldNotifications (
    @Parameter (description = "Number of days that constraints the deleting process", required = true)
    @PathVariable Long expirationDays) {
        notificationService.deleteOldNotifications(expirationDays);
        return ResponseEntity.noContent().build();
    }

    @Operation(
    summary = "Delete single notification",
    description = "Deletes a notification by its id")
    @DeleteMapping("/delete/single/{id}")
    public ResponseEntity <Void> deleteSingle (
    @Parameter(description = "ID of the app version to delete", required = true)
    @PathVariable Long id) {
        notificationService.deleteSingleNotification(id);
        return ResponseEntity.noContent().build();
    }

    @Operation (
    summary = "Send notifications to all outdated devices",
    description = "Sends notifications about a needing to update to all devices with MANDATORY or DEPRECATED app versions")
    @PostMapping("/send")
    public String sendToOutdated () {
        return "Notifications sent: " + notificationService.sendNotificationsToOutdatedDevices();
    }
}
