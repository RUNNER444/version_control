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

import lombok.RequiredArgsConstructor;

import com.example.demo.dto.AppVersionRequestDto;
import com.example.demo.dto.AppVersionResponseDto;
import com.example.demo.enums.PlatformType;
import com.example.demo.mapper.AppVersionMapper;
import com.example.demo.model.AppVersion;
import com.example.demo.repository.AppVersionRepository;
import com.example.demo.specifications.AppVersionSpecifications;

import jakarta.persistence.EntityNotFoundException;

@Service
@org.springframework.transaction.annotation.Transactional (readOnly = true)
@RequiredArgsConstructor
public class AppVersionService {
    private static final Logger logger = LoggerFactory.getLogger(AppVersionService.class);
    
    private final AppVersionRepository appVersionRepository;

    //CRUD

    @Cacheable (value = "appVersions", key = "#root.methodName")
    public List<AppVersionResponseDto> getAll() {;
        List <AppVersion> appVersions = appVersionRepository.findAll();
        List <AppVersionResponseDto> response = new ArrayList<>(); 

        for (AppVersion appVersion : appVersions) {
            response.add(AppVersionMapper.appVersionToAppVersionResponseDto(appVersion));
        }
        logger.info("Successfully retrieved all AppVersions. Total count: {}", response.size());
        
        return response;
    }

    @CacheEvict (value = "appVersions", allEntries = true)
    @Transactional
    public AppVersionResponseDto create (AppVersionRequestDto request) {
        logger.info("Creating new AppVersion: {} for platform: {}", request.version(), request.platform());
        
        AppVersion newAppVersion = appVersionRepository.save(new AppVersion(null, request.version(), request.platform(),
        null, LocalDateTime.now().toString() + ": created", request.updateType(), request.active()));
        logger.info("Successfully created AppVersion with ID: {} for platform: {}", newAppVersion.getId(), request.platform());
        return AppVersionMapper.appVersionToAppVersionResponseDto(newAppVersion);
    }

    @Cacheable (value = "appVersion", key = "#id")
    public AppVersionResponseDto getById(Long id) {
        AppVersion appVersion = appVersionRepository.findById(id).orElse(null);
        if (appVersion != null) {
            logger.info("Successfully retrieved AppVersion with ID: {}", id);
            return AppVersionMapper.appVersionToAppVersionResponseDto(appVersion);
        }

        throw new EntityNotFoundException("There's no AppVersion with ID: " + id.toString());
    }

    @Caching (evict = {
        @CacheEvict(value = "appVersions", allEntries = true),
        @CacheEvict(value = {"appVersions", "appVersion"}, key = "#id")
    })
    @Transactional
    public AppVersionResponseDto update(Long id, AppVersionRequestDto request) {
        AppVersion updated = appVersionRepository.findById(id).map(existingAppVersion -> {
            logger.debug("Values before update - version: {}, platform: {}, active: {}", 
                        existingAppVersion.getVersion(), existingAppVersion.getPlatform(), existingAppVersion.isActive());
            existingAppVersion.setVersion(request.version());
            existingAppVersion.setPlatform(request.platform());
            existingAppVersion.setChangelog(LocalDateTime.now().toString() + ": updated | " + existingAppVersion.getChangelog());
            existingAppVersion.setUpdateType(request.updateType());
            existingAppVersion.setActive(request.active());
            
            AppVersion savedVersion = appVersionRepository.save(existingAppVersion);
            logger.info("Successfully updated AppVersion with ID: {}", id);
            return savedVersion;
        }).orElse(null);
        if (updated == null) {
            throw new EntityNotFoundException("There's no AppVersion with ID: " + id.toString());
        }

        return AppVersionMapper.appVersionToAppVersionResponseDto(updated);
    }

    @Caching (evict = {
        @CacheEvict(value = "appVersions", allEntries = true),
        @CacheEvict(value = {"appVersions", "appVersion"}, key = "#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        if (appVersionRepository.existsById(id)) {
            appVersionRepository.deleteById(id);
            logger.info("Successfully deleted AppVersion with ID: {}", id);
            return true;
        }
        throw new EntityNotFoundException("There's no AppVersion with ID: " + id.toString());
    }

    public AppVersionResponseDto getByVersionAndPlatform(String version, PlatformType platformType) {
        AppVersion appVersion = appVersionRepository.findFirstByVersionAndPlatform(version, platformType);
        if (appVersion != null) {
            logger.info("Found exact same AppVersion in database: {}", version);
            return AppVersionMapper.appVersionToAppVersionResponseDto(appVersion);
        }
        throw new EntityNotFoundException("There's no AppVersion with version: " + version);
    }

    //LOGIC

    @Cacheable (value = "latestAppVersion", key = "#platform")
    public AppVersionResponseDto getLatestVersion(String platform) {
        PlatformType platformType;
        try {platformType = PlatformType.valueOf(platform.toUpperCase());}
        catch (IllegalArgumentException e) {throw new IllegalArgumentException("Invalid platform type provided. Must be one of the valid PlatformType enum values.");}
        
        AppVersion latest = appVersionRepository.findFirstByPlatformAndActiveTrueOrderByReleaseDateDesc(platformType);
        if (latest != null) {
            logger.info("Found latest AppVersion for platform {}: version {} released on {}", 
                       platformType, latest.getVersion(), latest.getReleaseDate());
            return AppVersionMapper.appVersionToAppVersionResponseDto(latest);
        }
        throw new EntityNotFoundException("There's no AppVersion with platform: " + platform);
    }

    public Page<AppVersion> getByFilter (String version, Pageable pageable) {
        Page <AppVersion> result = appVersionRepository.findAll(AppVersionSpecifications.filter(version), pageable);
        logger.info("Successfully filtered AppVersions. Found {} results", result.getNumberOfElements());
        return result;
    }
}
