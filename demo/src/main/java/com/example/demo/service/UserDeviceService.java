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
        return response;
    }

    @CacheEvict (value = "userDevices", allEntries = true)
    @Transactional
    public UserDeviceResponseDto create (UserDeviceRequestDto request) {
        UserDevice newUserDevice = userDeviceRepository.save(new UserDevice(null, request.userId(),
        request.platform(), request.currentVersion(), LocalDateTime.now()));
        return UserDeviceMapper.userDeviceToUserDeviceResponseDto(newUserDevice);
    }

    @Cacheable (value = "userDevices", key = "#id")
    public UserDeviceResponseDto getById(Long id) {
        UserDevice userDevice = userDeviceRepository.findById(id).orElse(null);
        if (userDevice != null) return UserDeviceMapper.userDeviceToUserDeviceResponseDto(userDevice);
        return null;
    }

    @Caching (evict = {
        @CacheEvict(value = "userDevices", allEntries = true),
        @CacheEvict(value = {"userDevices", "userDevice"}, key = "#id")
    })
    @Transactional
    public UserDeviceResponseDto update(Long id, UserDeviceRequestDto request) {
        UserDevice updated = userDeviceRepository.findById(id).map(existingUserDevice -> {
            existingUserDevice.setUserId(request.userId());
            existingUserDevice.setPlatform(request.platform());
            existingUserDevice.setCurrentVersion(request.currentVersion());
            existingUserDevice.setLastSeen(LocalDateTime.now());
            return userDeviceRepository.save(existingUserDevice);
        }).orElse(null);
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

    public List<UserDeviceResponseDto> getOutdatedDevices(Long userId, String platform) {
        AppVersionResponseDto latestAppVersion = appVersionService.getLatestVersion(platform);
        if (latestAppVersion == null) return List.of();

        PlatformType platformType = PlatformType.valueOf(platform.toUpperCase());
        List<UserDevice> allUserDevices = userDeviceRepository.findAllByPlatformAndUserId(platformType, userId);
        List<UserDeviceResponseDto> outdatedDevices = new ArrayList<>();
        
        for (UserDevice userDevice : allUserDevices) {
            if (!userDevice.getCurrentVersion().equals(latestAppVersion.version())) outdatedDevices.add(
                UserDeviceMapper.userDeviceToUserDeviceResponseDto(userDevice)
            );
        }
        return outdatedDevices;
    }
}
