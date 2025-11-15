package com.example.demo.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AppVersionRequestDto;
import com.example.demo.dto.AppVersionResponseDto;
import com.example.demo.service.AppVersionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;




@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AppVersionController {

    private final AppVersionService appVersionService;

    //CRUD

    @GetMapping("/appVersions")
    public List<AppVersionResponseDto> getAppVersions() {
        return appVersionService.getAll();
    }

    @GetMapping("/appVersions/{id}")
    public ResponseEntity<AppVersionResponseDto> getAppVersion(@PathVariable Long id) {
        return ResponseEntity.ok().body(appVersionService.getById(id));
    }
    

    @PostMapping("/appVersions")
    public ResponseEntity<AppVersionResponseDto> addAppVersion(@RequestBody @Valid AppVersionRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appVersionService.create(request));
    }
    
    @PutMapping("/appVersions/{id}")
    public ResponseEntity <AppVersionResponseDto> editAppVersion(@PathVariable Long id, @RequestBody @Valid AppVersionRequestDto request) {   
        AppVersionResponseDto updated = appVersionService.update(id, request);
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
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }

    //LOGIC

    @GetMapping("/appVersions/latest")
    public ResponseEntity<AppVersionResponseDto> getLatestAppVersion(@RequestParam (required = true) String platform) {
        AppVersionResponseDto latestVersion = appVersionService.getLatestVersion(platform);
        if (latestVersion != null) {
            return ResponseEntity.ok().body(latestVersion);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/appVersionFilter")
    public ResponseEntity<Object> getByFilter(@RequestParam (required = false) String version,
    @PageableDefault (page = 0, size = 10, sort = "version")
    Pageable pageable) {
        return ResponseEntity.ok(appVersionService.getByFilter(version, pageable));
    }
}