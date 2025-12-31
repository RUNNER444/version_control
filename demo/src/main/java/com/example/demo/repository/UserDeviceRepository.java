package com.example.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.enums.PlatformType;
import com.example.demo.model.UserDevice;
import com.example.demo.repository.projection.VersionDistribution;

public interface UserDeviceRepository extends 
    JpaRepository <UserDevice, Long>,
    JpaSpecificationExecutor <UserDevice> {
    List <UserDevice> findByUserId(Long userId);

    List <UserDevice> findAllByUserId(Long id);

    List <UserDevice> findAllByPlatformAndUserId(PlatformType platform, Long userId);
    
    @Query("SELECT d.currentVersion as version, d.platform as platform, COUNT(d) as usersCount FROM UserDevice d " + 
        "GROUP BY d.currentVersion, d.platform")
    List <VersionDistribution> getVersionDistrib();
}
