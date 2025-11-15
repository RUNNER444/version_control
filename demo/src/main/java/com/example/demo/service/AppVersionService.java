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
    private final AppVersionRepository appVersionRepository;

    //CRUD

    @Cacheable (value = "appVersions", key = "#root.methodName")
    public List<AppVersionResponseDto> getAll() {
        List <AppVersion> appVersions = appVersionRepository.findAll();
        List <AppVersionResponseDto> response = new ArrayList<>(); 
        for (AppVersion appVersion : appVersions) {
            response.add(AppVersionMapper.appVersionToAppVersionResponseDto(appVersion));
        }
        return response;
    }

    @CacheEvict (value = "appVersions", allEntries = true)
    @Transactional
    public AppVersionResponseDto create (AppVersionRequestDto request) {
        AppVersion newaAppVersion = appVersionRepository.save(new AppVersion(null, request.version(), request.platform(),
        LocalDateTime.now(), "", request.updateType(), request.active()));
        return AppVersionMapper.appVersionToAppVersionResponseDto(newaAppVersion);
    }

    @Cacheable (value = "appVersion", key = "#id")
    public AppVersionResponseDto getById(Long id) {
        AppVersion appVersion = appVersionRepository.findById(id).orElse(null);
        if (appVersion != null) return AppVersionMapper.appVersionToAppVersionResponseDto(appVersion);
        return null;
    }

    @Caching (evict = {
        @CacheEvict(value = "appVersions", allEntries = true),
        @CacheEvict(value = {"appVersions", "appVersion"}, key = "#id")
    })
    @Transactional
    public AppVersionResponseDto update(Long id, AppVersionRequestDto request) {
        AppVersion updated = appVersionRepository.findById(id).map(existingAppVersion -> {
            existingAppVersion.setVersion(request.version());
            existingAppVersion.setPlatform(request.platform());
            existingAppVersion.setChangelog(existingAppVersion.getChangelog() + "\n updated");
            existingAppVersion.setUpdateType(request.updateType());
            existingAppVersion.setActive(request.active());
            return appVersionRepository.save(existingAppVersion);
        }).orElse(null);
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
            return true;
        }
        else {
            return false;
        }
    }

    //LOGIC

    @Cacheable (value = "latestAppVersion", key = "#platform")
    public AppVersionResponseDto getLatestVersion(String platform) {
        PlatformType platformType = PlatformType.valueOf(platform.toUpperCase());
        AppVersion latest = appVersionRepository.findFirstByPlatformAndActiveTrueOrderByReleaseDateDesc(platformType);
        if (latest != null) return AppVersionMapper.appVersionToAppVersionResponseDto(latest);
        return null;
    }

    public Page<AppVersion> getByFilter (String version, Pageable pageable) {
        return appVersionRepository.findAll(AppVersionSpecifications.filter(version), pageable);
    }
}
