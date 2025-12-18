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
import jakarta.persistence.EntityNotFoundException;
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
        List <UserDevice> userDevices = userDeviceRepository.findAll();
        List <UserDeviceResponseDto> response = new ArrayList<>();
        
        for (UserDevice userDevice : userDevices) {
            response.add(UserDeviceMapper.userDeviceToUserDeviceResponseDto(userDevice));
        }
        logger.info("Successfully retrieved all UserDevices. Total count: {}", response.size());

        return response;
    }

    @CacheEvict (value = "userDevices", allEntries = true)
    @Transactional
    public UserDeviceResponseDto create (UserDeviceRequestDto request) {
        logger.info("Creating new UserDevice for user: {} ", request.userId());

        UserDevice newUserDevice = userDeviceRepository.save(new UserDevice(null, request.userId(), null,
        request.platform(), request.currentVersion(), null, null));
        logger.info("Successfully created UserDevice with ID: {} for user: {}", newUserDevice.getId(), request.userId());
        return UserDeviceMapper.userDeviceToUserDeviceResponseDto(newUserDevice);
    }

    @Cacheable (value = "userDevices", key = "#id")
    public UserDeviceResponseDto getById(Long id) {
        UserDevice userDevice = userDeviceRepository.findById(id).orElse(null);
        if (userDevice != null) {
            logger.info("Successfully retrieved UserDevice with ID: {}", id);
            return UserDeviceMapper.userDeviceToUserDeviceResponseDto(userDevice);
        }

        throw new EntityNotFoundException("There's no UserDevice with ID: " + id.toString());
    }

    @Caching (evict = {
        @CacheEvict(value = "userDevices", allEntries = true),
        @CacheEvict(value = {"userDevices", "userDevice"}, key = "#id")
    })
    @Transactional
    public UserDeviceResponseDto update(Long id, UserDeviceRequestDto request) {
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
            throw new EntityNotFoundException("There's no UserDevice with ID: " + id.toString());
        }

        return UserDeviceMapper.userDeviceToUserDeviceResponseDto(updated);
    }

    @Caching (evict = {
        @CacheEvict(value = "userDevices", allEntries = true),
        @CacheEvict(value = {"userDevices", "userDevice"}, key = "#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        if (userDeviceRepository.existsById(id)) {
            userDeviceRepository.deleteById(id);
            logger.info("Successfully deleted UserDevice with ID: {}", id);
            return true;
        }
        throw new EntityNotFoundException("There's no UserDevice with ID: " + id.toString());
    }

    @Caching (evict = {
        @CacheEvict(value = "userDevices", allEntries = true),
        @CacheEvict(value = {"userDevices", "userDevice"}, key = "#id")
    })
    @Transactional
    public UserDeviceResponseDto updateOnlyVersion (Long id, String version) {
        UserDevice updated = userDeviceRepository.findById(id).map(existingUserDevice -> {
            logger.debug("Values before update - userId: {}, platform: {}, currentVersion: {}, lastSeen: {}", 
                        existingUserDevice.getUserId(), existingUserDevice.getPlatform(), existingUserDevice.getCurrentVersion(), existingUserDevice.getLastSeen());
            existingUserDevice.setCurrentVersion(version);
            existingUserDevice.setLastSeen(LocalDateTime.now());

            UserDevice savedDevice = userDeviceRepository.save(existingUserDevice);
            logger.info("Successfully updated AppVersion on UserDevice with ID: {}", id);
            return savedDevice;
        }).orElse(null);
        if (updated == null) {
            throw new EntityNotFoundException("There's no UserDevice with ID: " + id.toString());
        }

        return UserDeviceMapper.userDeviceToUserDeviceResponseDto(updated);
    }

    //LOGIC

    public Page<UserDevice> getByFilter (Long userId, String version, Pageable pageable) {
        Page <UserDevice> result = userDeviceRepository.findAll(UserDeviceSpecifications.filter(userId, version), pageable);
        logger.info("Successfully filtered UserDEvices. Found {} results", result.getNumberOfElements());
        return result;
    }

    public List<UserDeviceResponseDto> getOutdatedDevices(Long userId, String platform) {
        AppVersionResponseDto latestAppVersion = appVersionService.getLatestVersion(platform);
        PlatformType platformType;
        try {platformType = PlatformType.valueOf(platform.toUpperCase());}
        catch (IllegalArgumentException e) {throw new IllegalArgumentException("Invalid platform type provided. Must be one of the valid PlatformType enum values.");}

        List<UserDevice> allUserDevices = userDeviceRepository.findAllByPlatformAndUserId(platformType, userId);
        List<UserDeviceResponseDto> outdatedDevices = new ArrayList<>();
        for (UserDevice userDevice : allUserDevices) {
            if (!userDevice.getCurrentVersion().equals(latestAppVersion.version())) outdatedDevices.add(
                UserDeviceMapper.userDeviceToUserDeviceResponseDto(userDevice)
            );
        }
        logger.info("Found all outdated UserDevices for User: {} with Platform: {}", userId, platform);
        return outdatedDevices;
    }
}
