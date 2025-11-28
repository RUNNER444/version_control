package com.example.demo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.AppVersionRequestDto;
import com.example.demo.dto.UploadResponseDto;
import com.example.demo.dto.UserDeviceRequestDto;
import com.example.demo.enums.PlatformType;
import com.example.demo.enums.UpdateType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadService {
    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    private final AppVersionService appVersionService;
    private final UserDeviceService userDeviceService;

    @Value("${spring.servlet.multipart.location}")
    private String uploadLocation;

    private void validateFile(MultipartFile file) {
        logger.debug("Attemting to validate file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("The system supports only CSV files");
        }

        logger.info("File validation passed: {}", filename);
    }

    private Path saveFile(MultipartFile file) throws IOException {
        logger.debug("Attempting to save file: {}", file.getOriginalFilename());

        String timestamp = LocalDateTime.now().toString().replaceAll(":", "-");
        String filename = timestamp + "_" + file.getOriginalFilename();

        Path targetLocation = Paths.get(uploadLocation).toAbsolutePath().normalize().resolve(filename);
        logger.debug("File copying target location is setted up");

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        logger.info("File saved to: {}", targetLocation);

        return targetLocation;
    }

    public UploadResponseDto importAppVersions (MultipartFile file) {
        logger.info("Starting import of AppVersions from file: {}", file.getOriginalFilename());

        validateFile(file);
        int successCount = 0;
        int failureCount = 0;

        try {
            Path savedFile = saveFile(file);

            CSVFormat format =
            CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).get();

            try (BufferedReader reader = Files.newBufferedReader(savedFile, StandardCharsets.UTF_8);
                 CSVParser csvParser = format.parse(reader)) {
                    int rowNumber = 1;
                    for (CSVRecord record : csvParser) {
                        try {
                            AppVersionRequestDto request = new AppVersionRequestDto(
                                record.get("version"),
                                PlatformType.valueOf(record.get("platform").toUpperCase()),
                                UpdateType.valueOf(record.get("updateType")),
                                Boolean.parseBoolean(record.get("active"))
                            );

                            appVersionService.create(request);
                            successCount++;

                            logger.debug("Successfully imported row {}: {}", rowNumber, request.version());
                        }
                        catch (Exception e) {
                            failureCount++;
                            logger.warn("Failed to import row {}: {}", rowNumber, e);
                        }
                        rowNumber++;
                    }
            }

            logger.info("AppVersion import completed. Success: {}, Failures: {}", successCount, failureCount);

            return new UploadResponseDto(successCount + failureCount,
                successCount,
                failureCount
            );
        }
        catch (IOException e) {
            logger.error("Error reading CSV file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage());
        }
    }

    public UploadResponseDto importUserDevices(MultipartFile file) {
        logger.info("Starting import of UserDevices from file: {}", file.getOriginalFilename());

        validateFile(file);
        int successCount = 0;
        int failureCount = 0;

        try {
            Path savedFile = saveFile(file);

            CSVFormat format =
            CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).get();

            try (BufferedReader reader = Files.newBufferedReader(savedFile, StandardCharsets.UTF_8);
                 CSVParser csvParser = format.parse(reader)) {
                    int rowNumber = 1;
                    for (CSVRecord record : csvParser) {
                        try {
                            UserDeviceRequestDto request = new UserDeviceRequestDto(
                                Long.parseLong(record.get("userId")),
                                PlatformType.valueOf(record.get("platform").toUpperCase()),
                                record.get("currentVersion")
                            );

                            userDeviceService.create(request);
                            successCount++;

                            logger.debug("Successfully imported row {}: {}", rowNumber, request.userId());
                        }
                        catch (Exception e) {
                            failureCount++;
                            logger.warn("Failed to import row {}: {}", rowNumber, e);
                        }
                        rowNumber++;
                    }
            }

            logger.info("UserDevice import completed. Success: {}, Failures: {}", successCount, failureCount);

            return new UploadResponseDto(successCount + failureCount,
                successCount,
                failureCount
            );
        }
        catch (IOException e) {
            logger.error("Error reading CSV file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage());
        }
    }
}
