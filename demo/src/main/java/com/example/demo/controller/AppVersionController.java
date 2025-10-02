package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.AppVersion;
import com.example.demo.service.AppVersionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/api")
public class AppVersionController {

    private final AppVersionService appVersionService;

    AppVersionController(AppVersionService appVersionService) {
        this.appVersionService = appVersionService;
    }

    @GetMapping("/appVersions")
    public List<AppVersion> getAppVersions() {
        return appVersionService.getAll();
    }

    @GetMapping("/appVersions/{id}")
    public ResponseEntity<AppVersion> getAppVersion(@PathVariable Long id) {
        return ResponseEntity.ok().body(appVersionService.getById(id));
    }
    

    @PostMapping("/appVersions")
    public ResponseEntity<AppVersion> addAppVersion(@RequestBody @Valid AppVersion appVersion) {
        AppVersion newAppVersion = appVersionService.create(appVersion);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAppVersion);
    }
    
    @PutMapping("/appVersions/{id}")
    public ResponseEntity <AppVersion> editAppVersion(@PathVariable Long id, @RequestBody @Valid AppVersion appVersion) {   
        AppVersion updated = appVersionService.update(id, appVersion);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping ("/appVersions/{id}")
    public ResponseEntity <Void> deleteAppVersion(@PathVariable Long id) {
        if (appVersionService.deleteById(id)) {
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }
}