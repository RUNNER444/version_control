package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
    private final UploadService uploadService;

    @Operation(
        summary = "Upload new App Versions",
        description = "Receives file with new App Versions and adds the to database")
    @PostMapping(value = "/appVersions",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity <UploadResponseDto> uploadAppVersions(@RequestParam MultipartFile file) {
        logger.info("Received request to upload file with AppVersions: {}", file.getOriginalFilename());

        try {
            UploadResponseDto response = uploadService.importAppVersions(file);

            if (response.failureCount() > 0) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }

            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            logger.warn("Invalid file upload request: {}", e.getMessage());

            return ResponseEntity.badRequest().body(new UploadResponseDto(
                0, 0, 0
            ));
        }
        catch (Exception e) {
            logger.error("Error while uploading file: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UploadResponseDto(
                0, 0, 0
            ));
        }
    }
    
    @Operation(
        summary = "Upload new User Devices",
        description = "Receives file with new User Devices and adds the to database")
    @PostMapping(value = "/userDevices",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity <UploadResponseDto> uploadUserDevices(@RequestParam MultipartFile file) {
        logger.info("Received request to upload file with UserDevices: {}", file.getOriginalFilename());

        try {
            UploadResponseDto response = uploadService.importUserDevices(file);

            if (response.failureCount() > 0) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }

            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            logger.warn("Invalid file upload request: {}", e.getMessage());

            return ResponseEntity.badRequest().body(new UploadResponseDto(
                0, 0, 0
            ));
        }
        catch (Exception e) {
            logger.error("Error while uploading file: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UploadResponseDto(
                0, 0, 0
            ));
        }
    }
}
