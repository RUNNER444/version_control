package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.AppVersion;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AppVersionController {
    private List<AppVersion> appVersions = new ArrayList<>();

    @GetMapping("/appVersions")
    public List<AppVersion> getAppVersions() {
        return appVersions;
    }

    @GetMapping("/appVersions/{version}")
    public ResponseEntity<AppVersion> getAppVersion(@PathVariable String version) {
        for (AppVersion appVersion : appVersions) {
            if (appVersion.getVersion().equals(version)) {
                return ResponseEntity.ok(appVersion);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/appVersions")
    public ResponseEntity<AppVersion> addAppVersion(@RequestBody @Valid AppVersion appVersion) {
        appVersion.setVersion("0.0.1");
        appVersions.add(appVersion);
        return ResponseEntity.status(HttpStatus.CREATED).body(appVersion);
    }
}