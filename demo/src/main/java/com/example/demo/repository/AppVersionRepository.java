package com.example.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.demo.enums.PlatformType;
import com.example.demo.model.AppVersion;

public interface AppVersionRepository extends 
    JpaRepository <AppVersion, Long>,
    JpaSpecificationExecutor <AppVersion> {
    List <AppVersion> findByVersionStartingWithIgnoreCase(String title);
    
    List <AppVersion> findAllByVersion(String version);
    
    AppVersion findTopByPlatformAndIsActiveOrderByReleaseDateDesc(PlatformType platform, boolean isActive);
}
