package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import com.example.demo.model.AppVersion;
import com.example.demo.repository.AppVersionRepository;

@Service
public class AppVersionService {
    private final AppVersionRepository appVersionRepository;
    
    public AppVersionService(AppVersionRepository appVersionRepository) {
        this.appVersionRepository = appVersionRepository;
    }

    private List <AppVersion> appVersions = new ArrayList<>();
    
    @PostConstruct
    public void init() {

    }

    public List<AppVersion> getAll() {
        return appVersionRepository.findAll();
    }

    public List <AppVersion> getAllByVersion (String version) {
        return appVersionRepository.findAllByVersion(version);
    }

    public AppVersion create (AppVersion appVersion) {
        return appVersionRepository.save(appVersion);
    }

    public AppVersion getById(Long id) {
        for (AppVersion appVersion : appVersions) {
            if (appVersion.getId().equals(id)) {
                return appVersionRepository.findById(id).orElse(null);
            }
        }
        return null;
    }

    public AppVersion update(Long id, AppVersion appVersion) {
        return appVersionRepository.findById(id).map(existingAppVersion -> {
            existingAppVersion.setVersion(appVersion.getVersion());
            return appVersionRepository.save(existingAppVersion);
        }).orElse(null);
    }

    public boolean deleteById(Long id) {
        if (appVersionRepository.existsById(id)) {
            appVersionRepository.deleteById(id);
            return true;
        }
        else {
            return false;
        }
    }
}
