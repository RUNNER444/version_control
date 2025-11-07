package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import com.example.demo.enums.PlatformType;
import com.example.demo.model.AppVersion;
import com.example.demo.model.UserDevice;
import com.example.demo.repository.UserDeviceRepository;
import com.example.demo.specifications.UserDeviceSpecifications;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional (readOnly = true)
public class UserDeviceService {
    private final UserDeviceRepository userDeviceRepository;
    private final AppVersionService appVersionService;
    
    @PostConstruct
    public void init() {}

    //CRUD

    @Cacheable (value = "userDevices", key = "#root.Methodname")
    public List<UserDevice> getAll() {
        return userDeviceRepository.findAll();
    }

    public List <UserDevice> getAllByUserId (Long id) {
        return userDeviceRepository.findAllByUserId(id);
    }

    @CacheEvict (value = "userDevices", allEntries = true)
    @Transactional
    public UserDevice create (UserDevice userDevice) {
        userDevice.setLastSeen(LocalDateTime.now());
        return userDeviceRepository.save(userDevice);
    }

    @Cacheable (value = "userDevices", key = "#id")
    public UserDevice getById(Long id) {
        return userDeviceRepository.findById(id).orElse(null);
    }

    @Caching (evict = {
        @CacheEvict(value = "userDevices", allEntries = true),
        @CacheEvict(value = {"userDevices", "userDevice"}, key = "#id")
    })
    @Transactional
    public UserDevice update(Long id, UserDevice userDevice) {
        return userDeviceRepository.findById(id).map(existingUserDevice -> {
            existingUserDevice.setUserId(userDevice.getUserId());
            existingUserDevice.setPlatform(userDevice.getPlatform());
            existingUserDevice.setCurrentVersion(userDevice.getCurrentVersion());
            existingUserDevice.setLastSeen(LocalDateTime.now());
            return userDeviceRepository.save(existingUserDevice);
        }).orElse(null);
    }

    @Caching (evict = {
        @CacheEvict(value = "userDevices", allEntries = true),
        @CacheEvict(value = {"userDevices", "userDevice"}, key = "#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        if (userDeviceRepository.existsById(id)) {
            userDeviceRepository.deleteById(id);
            return true;
        }
        else {
            return false;
        }
    }

    //LOGIC

    public Page<UserDevice> getByFilter (Long userId, String version, Pageable pageable) {
        return userDeviceRepository.findAll(UserDeviceSpecifications.filter(userId, version), pageable);
    }

    public List<UserDevice> getOutdatedDevices(Long userId, String platform) {
        AppVersion latestAppVersion = appVersionService.getLatestVersion(platform);
        if (latestAppVersion == null) return List.of();

        PlatformType platformType = PlatformType.valueOf(platform.toUpperCase());
        List<UserDevice> allDevices = userDeviceRepository.findAllByPlatformAndUserId(platformType, userId);
        List<UserDevice> outdatedDevices = new ArrayList<>();
        
        for (UserDevice device : allDevices) {
            if (!device.getCurrentVersion().equals(latestAppVersion.getVersion())) outdatedDevices.add(device);
        }
        return outdatedDevices;
    }
}
