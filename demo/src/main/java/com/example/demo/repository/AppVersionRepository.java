package com.example.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.AppVersion;

public interface AppVersionRepository extends JpaRepository <AppVersion, Long> {
    List <AppVersion> findByVersionStartingWithIgnoreCase(String title);
    List <AppVersion> findAllByVersion(String version);
}
