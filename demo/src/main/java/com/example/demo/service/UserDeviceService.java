package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import com.example.demo.dto.AppVersionResponseDto;
import com.example.demo.dto.UserDeviceRequestDto;
import com.example.demo.dto.UserDeviceResponseDto;
import com.example.demo.enums.PlatformType;
import com.example.demo.mapper.UserDeviceMapper;
import com.example.demo.model.UserDevice;
import com.example.demo.repository.UserDeviceRepository;
import com.example.demo.specifications.UserDeviceSpecifications;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional (readOnly = true)
public class UserDeviceService {
    private final UserDeviceRepository userDeviceRepository;
    private final AppVersionService appVersionService;
    private static final Logger logger = LoggerFactory.getLogger(UserDeviceService.class);
    
    @PostConstruct
    public void init() {}

    //CRUD

    @Cacheable (value = "userDevices", key = "#root.methodName")
    public List<UserDeviceResponseDto> getAll() {
        logger.debug("Attempting to retrieve all UserDevices from database");

        List <UserDevice> userDevices = userDeviceRepository.findAll();
        logger.debug("Retrieved {} UserDevices from database", userDevices.size());

        List <UserDeviceResponseDto> response = new ArrayList<>();
        for (UserDevice userDevice : userDevices) {
            response.add(UserDeviceMapper.userDeviceToUserDeviceResponseDto(userDevice));
        }
        logger.debug("Successfully mapped {} entities to DTOs", response.size());
        logger.info("Successfully retrieved all UserDevices. Total count: {}", response.size());

        return response;
    }

    @CacheEvict (value = "userDevices", allEntries = true)
    @Transactional
    public UserDeviceResponseDto create (UserDeviceRequestDto request) {
        logger.info("Creating new UserDevice for user: {} ", request.userId());

        UserDevice newUserDevice = userDeviceRepository.save(new UserDevice(null, request.userId(),
        request.platform(), request.currentVersion(), LocalDateTime.now()));
        logger.debug("UserDevice persisted to database with ID: {}", newUserDevice.getId());
        logger.info("Successfully created UserDevice with ID: {} for user: {}", newUserDevice.getId(), request.userId());

        return UserDeviceMapper.userDeviceToUserDeviceResponseDto(newUserDevice);
    }

    @Cacheable (value = "userDevices", key = "#id")
    public UserDeviceResponseDto getById(Long id) {
        logger.debug("Attempting to retrieve UserDevice with ID: {}", id);

        UserDevice userDevice = userDeviceRepository.findById(id).orElse(null);
        if (userDevice != null) {
            logger.info("Successfully retrieved UserDevice with ID: {}", id);

            return UserDeviceMapper.userDeviceToUserDeviceResponseDto(userDevice);
        }

        logger.warn("UserDevice with ID: {} not found in database", id);
        return null;
    }

    @Caching (evict = {
        @CacheEvict(value = "userDevices", allEntries = true),
        @CacheEvict(value = {"userDevices", "userDevice"}, key = "#id")
    })
    @Transactional
    public UserDeviceResponseDto update(Long id, UserDeviceRequestDto request) {
        logger.info("Attempting to update UserDevice with ID: {}", id);
        
        UserDevice updated = userDeviceRepository.findById(id).map(existingUserDevice -> {
            logger.debug("Values before update - userId: {}, platform: {}, currentVersion: {}, lastSeen: {}", 
                        existingUserDevice.getUserId(), existingUserDevice.getPlatform(), existingUserDevice.getCurrentVersion(), existingUserDevice.getLastSeen());
            
            existingUserDevice.setUserId(request.userId());
            existingUserDevice.setPlatform(request.platform());
            existingUserDevice.setCurrentVersion(request.currentVersion());
            existingUserDevice.setLastSeen(LocalDateTime.now());

            UserDevice savedDevice = userDeviceRepository.save(existingUserDevice);
            logger.info("Successfully updated UserDevice with ID: {}", id);
            
            return savedDevice;
        }).orElse(null);

        if (updated == null) {
            logger.warn("Failed to update UserDevice with ID: {} - entity not found", id);
        }

        return UserDeviceMapper.userDeviceToUserDeviceResponseDto(updated);
    }

    @Caching (evict = {
        @CacheEvict(value = "userDevices", allEntries = true),
        @CacheEvict(value = {"userDevices", "userDevice"}, key = "#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        logger.info("Attempting to delete UserDevice with ID: {}", id);

        if (userDeviceRepository.existsById(id)) {
            logger.debug("UserDevice with ID: {} exists, proceeding with deletion", id);

            userDeviceRepository.deleteById(id);
            logger.info("Successfully deleted UserDevice with ID: {}", id);

            return true;
        }
        else {
            logger.warn("Can't delete UserDevice with ID: {} - entity does not exist", id);
            return false;
        }
    }

    //LOGIC

    public Page<UserDevice> getByFilter (Long userId, String version, Pageable pageable) {
        logger.info("Attempting to filter UserDevices");

        Page <UserDevice> result = userDeviceRepository.findAll(UserDeviceSpecifications.filter(userId, version), pageable);
        logger.debug("Filter query returned {} results out of {} total elements", 
                    result.getNumberOfElements(), result.getTotalElements());
        logger.info("Successfully filtered UserDEvices. Found {} results", result.getNumberOfElements());

        return result;
    }

    public List<UserDeviceResponseDto> getOutdatedDevices(Long userId, String platform) {
        logger.info("Retrieving outdated UserDevices for User: {} with Platform: {}", userId, platform);
        
        try {
            logger.debug("Querying database for latest active AppVersion of platform: {}", platform);
            AppVersionResponseDto latestAppVersion = appVersionService.getLatestVersion(platform);
            if (latestAppVersion == null) {
                logger.warn("There are no active versions for platform");
                return List.of();
            } 

            PlatformType platformType = PlatformType.valueOf(platform.toUpperCase());

            logger.debug("Retrieving all user devices for platform from database");
            List<UserDevice> allUserDevices = userDeviceRepository.findAllByPlatformAndUserId(platformType, userId);
            List<UserDeviceResponseDto> outdatedDevices = new ArrayList<>();
            
            logger.debug("Searching outdated UserDevices through the retrieved list");
            for (UserDevice userDevice : allUserDevices) {
                if (!userDevice.getCurrentVersion().equals(latestAppVersion.version())) outdatedDevices.add(
                    UserDeviceMapper.userDeviceToUserDeviceResponseDto(userDevice)
                );
            }

            logger.info("Found all outdated UserDevices for User: {} with Platform: {}", userId, platform);
            return outdatedDevices;
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid platform or userId provided. Must be one of the valid PlatformType and existing userIds. Error: {}", 
                        e.getMessage(), e);
            
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving outdated UserDevices for User: {} with Platform: {}. Error: {}", 
                        userId, platform, e.getMessage(), e);
            throw e;
        }
    }
}
