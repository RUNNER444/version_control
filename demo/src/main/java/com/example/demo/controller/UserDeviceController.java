package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UserDeviceRequestDto;
import com.example.demo.dto.UserDeviceResponseDto;
import com.example.demo.service.ReportService;
import com.example.demo.service.UserDeviceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "User Devices", description = "Methods for managing user devices and their app versions")
public class UserDeviceController {
    private final UserDeviceService userDeviceService;
    private final ReportService reportService;

    //CRUD

    @Operation(
        summary = "Get All User Devices",
        description = "Retrieves a list of all user devices")
    @GetMapping("/userDevices")
    public ResponseEntity<List<UserDeviceResponseDto>> getUserDevices() {
        return ResponseEntity.ok().body(userDeviceService.getAll());
    }

    @Operation(
        summary = "Get User Device by ID",
        description = "Retrieves a specific user device by its unique identifier")
    @GetMapping("/userDevices/{id}")
    public ResponseEntity<UserDeviceResponseDto> getUserDevice(
    @Parameter(description = "ID of the user device", required = true)
    @PathVariable Long id) {
        return ResponseEntity.ok().body(userDeviceService.getById(id));
    }
    
    @Operation(
        summary = "Register New User Device",
        description = "Registers a new user device in the system")
    @PostMapping("/userDevices")
    public ResponseEntity<UserDeviceResponseDto> addUserDevice(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User device data to create", required = true)
    @RequestBody @Valid UserDeviceRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userDeviceService.create(request));
    }
    
    @Operation(
        summary = "Update User Device",
        description = "Updates an existing user device record")
    @PutMapping("/userDevices/{id}")
    public ResponseEntity <UserDeviceResponseDto> editUserDevice(
    @Parameter(description = "ID of the user device to update", required = true)
    @PathVariable Long id, 
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated user device data", required = true)
    @RequestBody @Valid UserDeviceRequestDto request) {   
        return ResponseEntity.ok(userDeviceService.update(id, request));
    }

    @Operation(
        summary = "Delete User Device",
        description = "Deletes a user device from the system")
    @DeleteMapping ("/userDevices/{id}")
    public ResponseEntity <Void> deleteUserDevice(
    @Parameter(description = "ID of the user device to delete", required = true)
    @PathVariable Long id) {
        userDeviceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    //LOGIC

    @Operation(
        summary = "Get user's Outdated Devices",
        description = "Finds all devices of specified user that are not running the latest version")
    @GetMapping("/userDevices/check")
    public ResponseEntity<List<UserDeviceResponseDto>> checkOutdatedDevices(
    @Parameter(description = "ID of the user for checking devices", required = true)
    @RequestParam(required = true) Long userId,
    @Parameter(description = "Platform type (ANDROID or IOS)", required = true)
    @RequestParam(required = true) String platform) {
        return ResponseEntity.ok(userDeviceService.getOutdatedDevices(userId, platform));
    }

    @Operation(
        summary = "Generate report for user's outdated devices",
        description = "Packs all user's outdated devices into single html file with all important information about them")
    @GetMapping(value = "/userDevices/getReport")
    public ResponseEntity<Resource> downloadOutdatedForUser(
    @Parameter(description = "ID of the user for checking devices", required = true)
    @RequestParam(required = true) Long userId,
    @Parameter(description = "Platform type (ANDROID or IOS)", required = true)
    @RequestParam(required = true) String platform) {
        Resource resource = reportService.getOutdatedDevicesReport(userId, platform);
        return ResponseEntity.ok()
        .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=outdated_report.xlsx")
        .body(resource);
    }

    @Operation(
    summary = "Filter User Devices",
    description = "Search and filter user devices")
    @GetMapping("/userDeviceFilter")
    public ResponseEntity<Object> getByFilter(
    @Parameter(description = "ID of the user to filter by")
    @RequestParam (required = false) Long userId,
    @Parameter(description = "Version to filter by", example = "1.0.0")
    @RequestParam (required = false) String version,
    @PageableDefault (page = 0, size = 10, sort = "userId")
    Pageable pageable) {
        return ResponseEntity.ok(userDeviceService.getByFilter(userId, version, pageable));
    }
}