package com.example.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;




@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "App Versions", description = "Methods for managing app versions across different platforms")
public class AppVersionController {
    private static final Logger logger = LoggerFactory.getLogger(AppVersionController.class);
    private final AppVersionService appVersionService;

    //CRUD

    @Operation(
    summary = "Get All App Versions",
    description = "Retrieves a list of all app versions registered in the system")
    @GetMapping("/appVersions")
    public ResponseEntity<List<AppVersionResponseDto>> getAppVersions() {
        logger.info("Received request to get all AppVersions");
        
        try {
            return ResponseEntity.ok().body(appVersionService.getAll());
        }
        catch (Exception e) {
            logger.error("Error while getting all AppVersions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
    summary = "Get App Version by ID",
    description = "Retrieves a specific app version by its unique identifier")
    @GetMapping("/appVersions/{id}")
    public ResponseEntity<AppVersionResponseDto> getAppVersion(
    @Parameter(description = "ID of the app version to retrieve", required = true)
    @PathVariable Long id) {
        logger.info("Received request to get AppVersion with id: {}", id);

        try {
            return ResponseEntity.ok().body(appVersionService.getById(id));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Id value is invalid: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while getting AppVersion with id: {}. Error: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @Operation(
    summary = "Create New App Version",
    description = "Creates a new app version in the system")
    @PostMapping("/appVersions")
    public ResponseEntity<AppVersionResponseDto> addAppVersion(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "App version data to create", required = true)
    @RequestBody @Valid AppVersionRequestDto request) {
        logger.info("Received request to add new AppVersion");

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(appVersionService.create(request));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Invalid add AppVersion request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
        catch (Exception e) {
            logger.error("Error while adding new AppVersion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @Operation(
    summary = "Update App Version",
    description = "Updates an existing app version")
    @PutMapping("/appVersions/{id}")
    public ResponseEntity <AppVersionResponseDto> editAppVersion(
    @Parameter(description = "ID of the app version to update", required = true)
    @PathVariable Long id,
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated app version data", required = true)
    @RequestBody @Valid AppVersionRequestDto request) {   
        logger.info("Received request to edit AppVersion with id: {}", id);
        
        try {
            AppVersionResponseDto updated = appVersionService.update(id, request);
            return ResponseEntity.ok(updated);
        }
        catch (IllegalArgumentException e) {
            logger.warn("Id value is invalid: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while updating AppVersion with id: {}. Error: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
    summary = "Delete App Version",
    description = "Deletes an app version from the system")
    @DeleteMapping ("/appVersions/{id}")
    public ResponseEntity <Void> deleteAppVersion(
        @Parameter(description = "ID of the app version to delete", required = true)
        @PathVariable Long id) {
        logger.info("Received request to delete AppVersion with id: {}", id);

        try {
            if (appVersionService.deleteById(id)) {
                return ResponseEntity.noContent().build();
            }

            logger.warn("Can't delete AppVersion with id: {}", id);
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while deleting AppVersion with id: {}. Error: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    //LOGIC

    @Operation(
    summary = "Get Latest App Version",
    description = "Retrieves the most recent active version for a specific platform")
    @GetMapping("/appVersions/latest")
    public ResponseEntity<AppVersionResponseDto> getLatestAppVersion(
        @Parameter(description = "Platform type (ANDROID or IOS)", required = true)
        @RequestParam (required = true) String platform) {
        logger.info("Received request to get the latest AppVersion for platform: {}", platform);
        
        try {
            AppVersionResponseDto latestVersion = appVersionService.getLatestVersion(platform);
            if (latestVersion != null) {
                return ResponseEntity.ok().body(latestVersion);
            }

            logger.warn("Can't get the latest version for platform: {}", platform);
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while getting the latest AppVersion for platform: {}. Error: {}", platform, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @Operation(
    summary = "Filter App Versions",
    description = "Search and filter app versions")
    @GetMapping("/appVersionFilter")
    public ResponseEntity<Object> getByFilter(
    @Parameter(description = "Version to filter by", example = "1.0.0")
    @RequestParam (required = false) String version,
    @PageableDefault (page = 0, size = 10, sort = "version")
    Pageable pageable) {
        logger.info("Received request to get AppVersions with version: {}", version);

        try {
            return ResponseEntity.ok(appVersionService.getByFilter(version, pageable));
        }
        catch (Exception e) {
            logger.error("Error while getting AppVersions with version: {}. Error: {}", version, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}