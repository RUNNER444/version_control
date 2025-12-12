package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.enums.NotificationType;
import com.example.demo.enums.UpdateType;
import com.example.demo.model.Notification;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface NotificationRepository extends JpaRepository <Notification, Long> {
    List <Notification> findByDeviceIdOrderByCreatedAtDesc (Long deviceId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByUserIdAndStatus(Long userId, NotificationType status);

    List<Notification> findByStatusAndCreatedAtBefore(NotificationType status, LocalDateTime date);

    boolean existsByDeviceIdAndCurrentVersionAndLatestVersionAndUpdateType(
    Long deviceId, String currentVersion, String latestVersion, UpdateType updateType);
}
