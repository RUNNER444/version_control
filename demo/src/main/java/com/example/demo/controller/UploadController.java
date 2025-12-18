package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.UploadResponseDto;
import com.example.demo.service.UploadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "Methods for uploading .csv files to add info to database")
public class UploadController {
    private final UploadService uploadService;

    @Operation(
        summary = "Upload new App Versions",
        description = "Receives file with new App Versions and adds the to database")
    @PostMapping(value = "/appVersions",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity <UploadResponseDto> uploadAppVersions(@RequestParam MultipartFile file) {
        UploadResponseDto response = uploadService.importAppVersions(file);
        if (response.failureCount() > 0) return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Upload new User Devices",
        description = "Receives file with new User Devices and adds the to database")
    @PostMapping(value = "/userDevices",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity <UploadResponseDto> uploadUserDevices(@RequestParam MultipartFile file) {
        UploadResponseDto response = uploadService.importUserDevices(file);
        if (response.failureCount() > 0) return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
        return ResponseEntity.ok(response);
    }
}
