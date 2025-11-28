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

@Service
@org.springframework.transaction.annotation.Transactional (readOnly = true)
@RequiredArgsConstructor
public class AppVersionService {
    private static final Logger logger = LoggerFactory.getLogger(AppVersionService.class);
    
    private final AppVersionRepository appVersionRepository;

    //CRUD

    @Cacheable (value = "appVersions", key = "#root.methodName")
    public List<AppVersionResponseDto> getAll() {
        logger.debug("Attempting to retrieve all AppVersions from database");

        List <AppVersion> appVersions = appVersionRepository.findAll();
        logger.debug("Retrieved {} AppVersions from database", appVersions.size());
        
        List <AppVersionResponseDto> response = new ArrayList<>(); 
        for (AppVersion appVersion : appVersions) {
            response.add(AppVersionMapper.appVersionToAppVersionResponseDto(appVersion));
        }
        logger.debug("Successfully mapped {} entities to DTOs", response.size());
        logger.info("Successfully retrieved all AppVersions. Total count: {}", response.size());
        
        return response;
    }

    @CacheEvict (value = "appVersions", allEntries = true)
    @Transactional
    public AppVersionResponseDto create (AppVersionRequestDto request) {
        logger.info("Creating new AppVersion: {} for platform: {}", request.version(), request.platform());
        
        AppVersion newaAppVersion = appVersionRepository.save(new AppVersion(null, request.version(), request.platform(),
        LocalDateTime.now(), LocalDateTime.now().toString() + ": created", request.updateType(), request.active()));
        logger.debug("AppVersion persisted to database with ID: {}", newaAppVersion.getId());
        logger.info("Successfully created AppVersion with ID: {} for platform: {}", newaAppVersion.getId(), request.platform());

        return AppVersionMapper.appVersionToAppVersionResponseDto(newaAppVersion);
    }

    @Cacheable (value = "appVersion", key = "#id")
    public AppVersionResponseDto getById(Long id) {
        logger.debug("Attempting to retrieve AppVersion with ID: {}", id);
        
        AppVersion appVersion = appVersionRepository.findById(id).orElse(null);
        
        if (appVersion != null) {
            logger.info("Successfully retrieved AppVersion with ID: {}", id);
            
            return AppVersionMapper.appVersionToAppVersionResponseDto(appVersion);
        }

        logger.warn("AppVersion with ID: {} not found in database", id);
        return null;
    }

    @Caching (evict = {
        @CacheEvict(value = "appVersions", allEntries = true),
        @CacheEvict(value = {"appVersions", "appVersion"}, key = "#id")
    })
    @Transactional
    public AppVersionResponseDto update(Long id, AppVersionRequestDto request) {
        logger.info("Attempting to update AppVersion with ID: {}", id);
        
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
            logger.warn("Failed to update AppVersion with ID: {} - entity not found", id);
        }

        return AppVersionMapper.appVersionToAppVersionResponseDto(updated);
    }

    @Caching (evict = {
        @CacheEvict(value = "appVersions", allEntries = true),
        @CacheEvict(value = {"appVersions", "appVersion"}, key = "#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        logger.info("Attempting to delete AppVersion with ID: {}", id);
        
        if (appVersionRepository.existsById(id)) {
            logger.debug("AppVersion with ID: {} exists, proceeding with deletion", id);
            
            appVersionRepository.deleteById(id);
            logger.info("Successfully deleted AppVersion with ID: {}", id);
            
            return true;
        }

        else {
            logger.warn("Can't delete AppVersion with ID: {} - entity does not exist", id);
            return false;
        }
    }

    //LOGIC

    @Cacheable (value = "latestAppVersion", key = "#platform")
    public AppVersionResponseDto getLatestVersion(String platform) {
        logger.info("Retrieving latest active AppVersion for platform: {}", platform);
        
        try {
            logger.debug("Converting platform string '{}' to PlatformType enum", platform);
            PlatformType platformType = PlatformType.valueOf(platform.toUpperCase());
            
            logger.debug("Querying database for latest active AppVersion of platform: {}", platformType);
            AppVersion latest = appVersionRepository.findFirstByPlatformAndActiveTrueOrderByReleaseDateDesc(platformType);
            
            if (latest != null) {
                logger.info("Found latest AppVersion for platform {}: version {} released on {}", 
                           platformType, latest.getVersion(), latest.getReleaseDate());
                
                return AppVersionMapper.appVersionToAppVersionResponseDto(latest);
            }
            
            logger.warn("No active AppVersion found for platform: {}", platformType);
            return null;
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid platform type provided: '{}'. Must be one of the valid PlatformType enum values. Error: {}", 
                        platform, e.getMessage(), e);
            
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error occurred while retrieving latest AppVersion for platform: {}. Error: {}", 
                        platform, e.getMessage(), e);
            throw e;
        }
    }

    public Page<AppVersion> getByFilter (String version, Pageable pageable) {
        logger.info("Attempting to filter AppVersions");
        
        Page <AppVersion> result = appVersionRepository.findAll(AppVersionSpecifications.filter(version), pageable);
        logger.debug("Filter query returned {} results out of {} total elements", 
                    result.getNumberOfElements(), result.getTotalElements());
        logger.info("Successfully filtered AppVersions. Found {} results on page {} of {}", 
                   result.getNumberOfElements(), result.getNumber() + 1, result.getTotalPages());
        
        return result;
    }
}
