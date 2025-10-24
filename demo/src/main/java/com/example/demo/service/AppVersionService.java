package com.example.demo.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import com.example.demo.enums.PlatformType;
import com.example.demo.model.AppVersion;
import com.example.demo.repository.AppVersionRepository;
import com.example.demo.specifications.AppVersionSpecifications;

@Service
@org.springframework.transaction.annotation.Transactional (readOnly = true)
public class AppVersionService {
    private final AppVersionRepository appVersionRepository;
    
    public AppVersionService(AppVersionRepository appVersionRepository) {
        this.appVersionRepository = appVersionRepository;
    }
    
    @PostConstruct
    public void init() {

    }

    //CRUD

    @Cacheable (value = "appVersions", key = "#root.Methodname")
    public List<AppVersion> getAll() {
        return appVersionRepository.findAll();
    }

    public List <AppVersion> getAllByVersion (String version) {
        return appVersionRepository.findAllByVersion(version);
    }

    @CacheEvict (value = "appVersions", allEntries = true)
    @Transactional
    public AppVersion create (AppVersion appVersion) {
        return appVersionRepository.save(appVersion);
    }

    @Cacheable (value = "appVersion", key = "#id")
    public AppVersion getById(Long id) {
        return appVersionRepository.findById(id).orElse(null);
    }

    @Caching (evict = {
        @CacheEvict(value = "appVersions", allEntries = true),
        @CacheEvict(value = {"appVersions", "appVersion"}, key = "#id")
    })
    @Transactional
    public AppVersion update(Long id, AppVersion appVersion) {
        return appVersionRepository.findById(id).map(existingAppVersion -> {
            existingAppVersion.setVersion(appVersion.getVersion());
            existingAppVersion.setPlatform(appVersion.getPlatform());
            existingAppVersion.setReleaseDate(appVersion.getReleaseDate());
            existingAppVersion.setChangelog(appVersion.getChangelog());
            existingAppVersion.setUpdateType(appVersion.getUpdateType());
            existingAppVersion.setActive(appVersion.isActive());
            return appVersionRepository.save(existingAppVersion);
        }).orElse(null);
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

    @Cacheable (value = "latestAppVersion", key = "#platform.name()")
    public AppVersion getLatestVersion(PlatformType platform) {
        return appVersionRepository.findTopByPlatformAndIsActiveOrderByReleaseDateDesc(platform, true);
    }

    public Page<AppVersion> getByFilter (String version, Pageable pageable) {
        return appVersionRepository.findAll(AppVersionSpecifications.filter(version), pageable);
    }
}
