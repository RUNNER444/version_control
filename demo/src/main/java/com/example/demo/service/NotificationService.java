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

import jakarta.persistence.EntityNotFoundException;
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
        List <Notification> notifications = notificationRepository.findByDeviceIdOrderByCreatedAtDesc(id);
        List <NotificationResponseDto> response = new ArrayList<>();
        for (Notification notification : notifications) {
            response.add(NotificationMapper.notificationToNotificationResponseDto(notification));
        }
        logger.info("Successfully retrieved all notifications for device {}. Total count: {}", id, response.size());
            
        return response;
    }

    public List<NotificationResponseDto> getNotificationsByUserId (Long id) {
        List <Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(id);
        List <NotificationResponseDto> response = new ArrayList<>();
        for (Notification notification : notifications) {
            response.add(NotificationMapper.notificationToNotificationResponseDto(notification));
        }
        logger.info("Successfully retrieved all notifications for user {}. Total count: {}", id, response.size());

        return response;
    }

    public List<NotificationResponseDto> getUnreadNotificationsByUserId (Long id) {
        List <Notification> notifications = notificationRepository.findByUserIdAndStatus(id, NotificationType.PENDING);
        notifications.addAll(notificationRepository.findByUserIdAndStatus(id, NotificationType.SENT));
            
        List <NotificationResponseDto> response = new ArrayList<>();
        for (Notification notification : notifications) {
            response.add(NotificationMapper.notificationToNotificationResponseDto(notification));
        }
        logger.info("Successfully retrieved all unread notifications for user {}. Total count: {}", id, response.size());
        
        return response;
    }

    @Transactional
    public NotificationResponseDto createNotification (NotificationRequestDto request) {
        if (notificationRepository.existsByDeviceIdAndCurrentVersionAndLatestVersionAndUpdateType(
        request.deviceId(), request.currentVersion(), request.latestVersion(), request.updateType())) {
            logger.info("Similar notification already exists for device {}", request.deviceId());
            return null;
        }
        
        Notification newNotification = notificationRepository.save(new Notification(null, request.deviceId(), null, request.userId(),
        null, request.currentVersion(), null, request.latestVersion(), null,
        request.updateType(), NotificationType.PENDING, null, null, request.message()));
        logger.info("Update notification created. ID: {}", newNotification.getId());

        return NotificationMapper.notificationToNotificationResponseDto(newNotification);
    }

    @Transactional
    public NotificationResponseDto markAsRead (Long id) {
        Notification updated = notificationRepository.findById(id).map(
        existingNotification -> {
            existingNotification.setStatus(NotificationType.READ);
            existingNotification.setReadAt(LocalDateTime.now());
            logger.info("Successfully marked notification {} as read", id);

            return notificationRepository.save(existingNotification);
        }).orElse(null);
        
        if (updated == null) {
            throw new EntityNotFoundException("There's no notification with id " + id.toString());
        }

        return NotificationMapper.notificationToNotificationResponseDto(updated);
    }

    @Transactional
    public NotificationResponseDto markAsDismissed (Long id) {
        Notification updated = notificationRepository.findById(id).map(
        existingNotification -> {
            existingNotification.setStatus(NotificationType.DISMISSED);
            existingNotification.setReadAt(LocalDateTime.now());
            logger.info("Successfully marked notification {} as dismissed", id);

            return notificationRepository.save(existingNotification);
        }).orElse(null);
        
        if (updated == null) {
            throw new EntityNotFoundException("There's no notification with id " + id.toString());
        }

        return NotificationMapper.notificationToNotificationResponseDto(updated);
    }

    @Transactional
    public void deleteOldNotifications (Long expirationDays) {
        LocalDateTime limit = LocalDateTime.now().minusDays(expirationDays);
        notificationRepository.deleteAll(notificationRepository.findByStatusAndCreatedAtBefore(NotificationType.READ, limit));
        notificationRepository.deleteAll(notificationRepository.findByStatusAndCreatedAtBefore(NotificationType.DISMISSED, limit));
        logger.info("Successfully deleted old notifications");
    }

    @Transactional
    public boolean deleteSingleNotification (Long id) {
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
            logger.info("Successfully deleted notification with ID: {}", id);
            return true;
        }
        throw new EntityNotFoundException("There's no notification with ID: " + id.toString());
    }

    //LOGIC
    
    @Transactional
    public String sendNotificationsToOutdatedDevices() {
        int successCount = 0;
        List <UpdateResponseDto> outdatedDevices = updateService.getDevicesWithUpdateType("MANDATORY");
        outdatedDevices.addAll(updateService.getDevicesWithUpdateType("DEPRECATED"));

        for (UpdateResponseDto updateRecord : outdatedDevices) {
            try {
            createNotification(new NotificationRequestDto(updateRecord.userDeviceId(), updateRecord.userId(),
            updateRecord.currentVersion(), updateRecord.latestVersion(), updateRecord.updateType(), generateMessage(
                updateRecord.updateType(), updateRecord.latestVersion()
            ))); successCount++;}
            catch (Exception e) {
                logger.warn("Failed to send notification to device {}", updateRecord.userDeviceId());
            }
        }
        logger.info("Created {} notifications for outdated devices", successCount);
        return "Sent " + successCount + " notifications";
    }

    private String generateMessage (UpdateType updateType, String latestVersion) {
        if (updateType == UpdateType.DEPRECATED) {
            return "Your app version is no longer supported. Update to "  + latestVersion + " immediately.";
        }
        if (updateType == UpdateType.MANDATORY) {
            return "Your app version is outdated. Update to "  + latestVersion + " is recommended.";
        }
        if (updateType == UpdateType.OPTIONAL) {
            return "An optional update to "  + latestVersion + " is available.";
        }
        return "Your version is the latest. IDK who sent you this notification 0_o";
    }
}
