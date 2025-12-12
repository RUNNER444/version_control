package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.NotificationRequestDto;
import com.example.demo.dto.NotificationResponseDto;
import com.example.demo.dto.UpdateResponseDto;
import com.example.demo.enums.NotificationType;
import com.example.demo.enums.UpdateType;
import com.example.demo.mapper.NotificationMapper;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional (readOnly = true)
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UpdateService updateService;
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    //CRUD

    public List<NotificationResponseDto> getNotificationsByDeviceId (Long id) {
        logger.debug("Attempting to retrieve all notifications for device {} from database", id);

        try {
            List <Notification> notifications = notificationRepository.findByDeviceIdOrderByCreatedAtDesc(id);
            logger.debug("Retrieved {} notifications from database", notifications.size());

            List <NotificationResponseDto> response = new ArrayList<>();
            for (Notification notification : notifications) {
                response.add(NotificationMapper.notificationToNotificationResponseDto(notification));
            }
            logger.debug("Successfully mapped {} entities to DTOs", response.size());
            logger.info("Successfully retrieved all notifications for device {}. Total count: {}", id, response.size());
            
            return response;
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid id is provided. Must be a number. Error: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error occured while getting device's notifications. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<NotificationResponseDto> getNotificationsByUserId (Long id) {
        logger.debug("Attempting to retrieve all notifications for user {} from database", id);

        try {
            List <Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(id);
            logger.debug("Retrieved {} notifications from database", notifications.size());

            List <NotificationResponseDto> response = new ArrayList<>();
            for (Notification notification : notifications) {
                response.add(NotificationMapper.notificationToNotificationResponseDto(notification));
            }
            logger.debug("Successfully mapped {} entities to DTOs", response.size());
            logger.info("Successfully retrieved all notifications for user {}. Total count: {}", id, response.size());

            return response;
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid id is provided. Must be a number. Error: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error occured while getting user's notifications. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<NotificationResponseDto> getUnreadNotificationsByUserId (Long id) {
        logger.debug("Attempting to retrieve all unread notifications for user {} from database", id);

        try {
            List <Notification> notifications = notificationRepository.findByUserIdAndStatus(id, NotificationType.PENDING);
            notifications.addAll(notificationRepository.findByUserIdAndStatus(id, NotificationType.SENT));
            logger.debug("Retrieved {} notifications from database", notifications.size());
            
            List <NotificationResponseDto> response = new ArrayList<>();
            for (Notification notification : notifications) {
                response.add(NotificationMapper.notificationToNotificationResponseDto(notification));
            }
            logger.debug("Successfully mapped {} entities to DTOs", response.size());
            logger.info("Successfully retrieved all unread notifications for user {}. Total count: {}", id, response.size());
        
            return response;
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid id is provided. Must be a number. Error: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error occured while getting user's unread notifications. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public NotificationResponseDto createNotification (NotificationRequestDto request) {
        logger.info("Creating update notification for device {}", request.deviceId());

        try {
            if (notificationRepository.existsByDeviceIdAndCurrentVersionAndLatestVersionAndUpdateType(
            request.deviceId(), request.currentVersion(), request.latestVersion(), request.updateType())) {
                logger.info("Similar notification already exists for device {}", request.deviceId());
                return null;
            }
        
            Notification newNotification = notificationRepository.save(new Notification(null, request.deviceId(), request.userId(),
            request.currentVersion(), request.latestVersion(), request.updateType(), NotificationType.PENDING,
            LocalDateTime.now(), null, request.message()));
            logger.info("Update notification created. ID: {}", newNotification.getId());

            return NotificationMapper.notificationToNotificationResponseDto(newNotification);
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid data is provided for creating notification. Error: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error occured while creating notification. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public NotificationResponseDto markAsRead (Long id) {
        logger.debug("Marking notification {} as read", id);

        try {
            Notification updated = notificationRepository.findById(id).map(
            existingNotification -> {
                existingNotification.setStatus(NotificationType.READ);
                existingNotification.setReadAt(LocalDateTime.now());
                logger.info("Successfully marked notification {} as read", id);

                return notificationRepository.save(existingNotification);
            }).orElse(null);
        
            if (updated == null) {
                logger.warn("Failed to mark notification {} as read. Notification not found", id);
            }

            return NotificationMapper.notificationToNotificationResponseDto(updated);
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid id is provided. Must be a number. Error: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error occured while changing notification status. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public NotificationResponseDto markAsDismissed (Long id) {
        logger.debug("Marking notification {} as dismissed", id);

        try {
            Notification updated = notificationRepository.findById(id).map(
            existingNotification -> {
                existingNotification.setStatus(NotificationType.DISMISSED);
                existingNotification.setReadAt(LocalDateTime.now());
                logger.info("Successfully marked notification {} as dismissed", id);

                return notificationRepository.save(existingNotification);
            }).orElse(null);
        
            if (updated == null) {
                logger.warn("Failed to mark notification {} as dismissed. Notification not found", id);
            }

            return NotificationMapper.notificationToNotificationResponseDto(updated);
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid id is provided. Must be a number. Error: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error occured while changing notification status. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void deleteOldNotifications (Long expirationDays) {
        logger.info("Deleting notification that are older than {} days", expirationDays);

        try {
            LocalDateTime limit = LocalDateTime.now().minusDays(expirationDays);
            notificationRepository.deleteAll(notificationRepository.findByStatusAndCreatedAtBefore(NotificationType.READ, limit));
            notificationRepository.deleteAll(notificationRepository.findByStatusAndCreatedAtBefore(NotificationType.DISMISSED, limit));
            logger.info("Successfully deleted old notifications");
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid expiration date value is provided. Must be a number of days. Error: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error occured while deleting old notifications. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    //LOGIC

    public void sendNotificationsToOutdatedDevices() {
        logger.info("Sending notification to all outdated devices on all platforms");

        try {
            List <UpdateResponseDto> outdatedDevices = updateService.getDevicesWithUpdateType("MANDATORY");
            outdatedDevices.addAll(updateService.getDevicesWithUpdateType("DEPRECATED"));

            for (UpdateResponseDto updateRecord : outdatedDevices) {
                createNotification(new NotificationRequestDto(updateRecord.userDeviceId(), updateRecord.userId(),
                updateRecord.currentVersion(), updateRecord.latestVersion(), updateRecord.updateType(), generateMessage(
                    updateRecord.updateType(), updateRecord.latestVersion()
                )));
            }
            logger.info("Created {} notifications for outdated devices", outdatedDevices.size());
        }
        catch (Exception e) {
            logger.error("Unexpected error occured while sending notifications to outdated devices. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String generateMessage (UpdateType updateType, String latestVersion) {
        if (updateType == UpdateType.DEPRECATED) {
            return String.format("Your app version is no longer supported. Update to {} immediately.", latestVersion);
        }
        if (updateType == UpdateType.MANDATORY) {
            return String.format("Your app version is outdated. Update to {} is recommended.", latestVersion);
        }
        if (updateType == UpdateType.OPTIONAL) {
            return String.format("An optional update to version {} is available.", latestVersion);
        }
        return String.format("Your version is the latest. IDK who sent you this notification 0_o");
    }
}
