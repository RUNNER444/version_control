package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UserDeviceRequestDto;
import com.example.demo.dto.UserDeviceResponseDto;
import com.example.demo.service.UserDeviceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserDeviceController {
    private final UserDeviceService userDeviceService;
    private static final Logger logger = LoggerFactory.getLogger(UserDeviceController.class);

    //CRUD

    @GetMapping("/userDevices")
    public List<UserDeviceResponseDto> getUserDevices() {
        logger.info("Received request to get all UserDevices");

        try {
            return userDeviceService.getAll();
        }
        catch (Exception e) {
            logger.error("Error while getting all UserDevices: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @GetMapping("/userDevices/{id}")
    public ResponseEntity<UserDeviceResponseDto> getUserDevice(@PathVariable Long id) {
        logger.info("Received request to get UserDevice with id: {}", id);
        
        try {
            return ResponseEntity.ok().body(userDeviceService.getById(id));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Id value is invalid: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while getting UserDevice with id: {}. Error: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    

    @PostMapping("/userDevices")
    public ResponseEntity<UserDeviceResponseDto> addUserDevice(@RequestBody @Valid UserDeviceRequestDto request) {
        logger.info("Received request to add new UserDevice");
        
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userDeviceService.create(request));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Invalid add UserDevice request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
        catch (Exception e) {
            logger.error("Error while adding new UserDevice: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @PutMapping("/userDevices/{id}")
    public ResponseEntity <UserDeviceResponseDto> editUserDevice(@PathVariable Long id, @RequestBody @Valid UserDeviceRequestDto request) {   
        logger.info("Received request to edit UserDevice with id: {}", id);
        
        try {
            UserDeviceResponseDto updated = userDeviceService.update(id, request);
            return ResponseEntity.ok(updated);
        }
        catch (IllegalArgumentException e) {
            logger.warn("Id value is invalid: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while updating UserDevice with id: {}. Error: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping ("/userDevices/{id}")
    public ResponseEntity <Void> deleteUserDevice(@PathVariable Long id) {
        logger.info("Received request to delete UserDevice with id: {}", id);

        try {
            if (userDeviceService.deleteById(id)) {
                ResponseEntity.noContent().build();
            }

            logger.warn("Can't delete UserDevice with id: {}", id);
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while deleting UserDevice with id: {}. Error: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    //LOGIC

    @GetMapping("/userDevices/check")
    public ResponseEntity<List<UserDeviceResponseDto>> checkOutdatedDevices(@RequestParam(required = true) Long userId,
        @RequestParam(required = true) String platform) {
        logger.info("Received request to check outdated UserDevices with userId: {}", userId);
        
        try {
            List<UserDeviceResponseDto> outdatedDevices = userDeviceService.getOutdatedDevices(userId, platform);

            if (outdatedDevices != null) return ResponseEntity.ok(outdatedDevices);

            logger.info("All versions are up to date");
            return ResponseEntity.notFound().build();
        }
        catch (IllegalArgumentException e) {
            logger.warn("UserId value is invalid: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Error while checking outdated UserDevices with userId: {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/userDeviceFilter")
    public ResponseEntity<Object> getByFilter(@RequestParam (required = false) Long userId,
    @RequestParam (required = false) String version,
    @PageableDefault (page = 0, size = 10, sort = "userId")
    Pageable pageable) {
        logger.info("Received request to get filtered UserDevices");

        try {
            return ResponseEntity.ok(userDeviceService.getByFilter(userId, version, pageable));
        }
        catch (Exception e) {
            logger.error("Error while getting filtered UserDevices. Error: {}", version, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}