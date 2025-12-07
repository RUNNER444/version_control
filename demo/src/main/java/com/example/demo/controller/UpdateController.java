package com.example.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UpdateResponseDto;
import com.example.demo.service.UpdateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/update")
@Tag(name = "Updates", description = "Methods for checking updates and managing update requirements")
public class UpdateController {
    private final UpdateService updateService;
    private static final Logger logger = LoggerFactory.getLogger(UpdateController.class);

    @Operation(
    summary = "Check specific device for available updates",
    description = "Checks if a specific device needs to be updated based on its current version")
    @GetMapping("/single/check/{id}")
    public ResponseEntity<UpdateResponseDto> checkDeviceForUpdates (
        @Parameter (description = "ID of the device to check", required = true)
        @PathVariable Long id)
    {
        logger.info("Received request to check updates on device with id: {}", id);

        try {
            return ResponseEntity.ok().body(updateService.checkUserDeviceForUpdate(id));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Provided id value is invalid: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while checking device {} for updates. Error: {}",
                id, e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
    summary = "Get devices by update type",
    description = "Gets all devices which AppVersions have specified UpdateType")
    @GetMapping("/multiple/check/{type}")
    public ResponseEntity<List<UpdateResponseDto>> getDevicesByUpdate (
        @Parameter(description = "Update Type (UNAVAILABLE, OPTIONAL, MANDATORY or DEPRECATED)", required = true)
        @PathVariable String type
    )
    {
        logger.info("Received request to get all devices with AppVersion that has {} UpdateType", type);

        try {
            List <UpdateResponseDto> devicesWithType = updateService.getDevicesWithUpdateType(type);

            if (devicesWithType != null) {
                logger.info("Found {} devices with {} update type", devicesWithType.size(), type);
                return ResponseEntity.ok(devicesWithType);
            }

            logger.info("There are no devices which have app version with {} update type", type);
            return ResponseEntity.notFound().build();
        }
        catch (IllegalArgumentException e) {
            logger.warn("Provided update type is invalid. Error: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while getting devices with {} update type. Error: {}", type, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
    summary = "Update specific device",
    description = "Updates a specific device to the latest app version for its platform")
    @PutMapping("/single/{id}")
    public ResponseEntity<UpdateResponseDto> updateSingle (
        @Parameter (description = "ID of the device to update", required = true)
        @PathVariable Long id
    ) {
        logger.info("Received request to update device with id: {}", id);

        try {
            return ResponseEntity.ok().body(updateService.updateDevice(id));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Provided id value is invalid: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while updating device {} to latest version. Error: {}",
                id, e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
    summary = "Update all outdated devices",
    description = "Updates all devices that has an app version that is no longer supported or needs an mandatory update")
    @PutMapping("/multiple")
    public ResponseEntity<List<UpdateResponseDto>> forceUpdate () {
        logger.info("Received request to force update all outdated devices");

        try {
            return ResponseEntity.ok().body(updateService.forceUpdateAllOutdated());
        }
        catch (Exception e) {
            logger.error("Error while updating all outdated to latest version. Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
