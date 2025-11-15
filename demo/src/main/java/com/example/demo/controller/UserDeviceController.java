package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UserDeviceRequestDto;
import com.example.demo.dto.UserDeviceResponseDto;
import com.example.demo.model.UserDevice;
import com.example.demo.service.UserDeviceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

    //CRUD

    @GetMapping("/userDevices")
    public List<UserDeviceResponseDto> getUserDevices() {
        return userDeviceService.getAll();
    }

    @GetMapping("/userDevices/{id}")
    public ResponseEntity<UserDeviceResponseDto> getUserDevice(@PathVariable Long id) {
        return ResponseEntity.ok().body(userDeviceService.getById(id));
    }
    

    @PostMapping("/userDevices")
    public ResponseEntity<UserDeviceResponseDto> addUserDevice(@RequestBody @Valid UserDeviceRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userDeviceService.create(request));
    }
    
    @PutMapping("/userDevices/{id}")
    public ResponseEntity <UserDeviceResponseDto> editUserDevice(@PathVariable Long id, @RequestBody @Valid UserDeviceRequestDto request) {   
        UserDeviceResponseDto updated = userDeviceService.update(id, request);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping ("/userDevices/{id}")
    public ResponseEntity <Void> deleteUserDevice(@PathVariable Long id) {
        if (userDeviceService.deleteById(id)) {
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }

    //LOGIC

    @GetMapping("/userDevices/check")
    public ResponseEntity<List<UserDeviceResponseDto>> checkOutdatedDevices(@RequestParam(required = true) Long userId,
        @RequestParam(required = true) String platform) {
        List<UserDeviceResponseDto> outdatedDevices = userDeviceService.getOutdatedDevices(userId, platform);
        if (outdatedDevices != null) return ResponseEntity.ok(outdatedDevices);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/userDeviceFilter")
    public ResponseEntity<Object> getByFilter(@RequestParam (required = false) Long userId,
    @RequestParam (required = false) String version,
    @PageableDefault (page = 0, size = 10, sort = "userId")
    Pageable pageable) {
        return ResponseEntity.ok(userDeviceService.getByFilter(userId, version, pageable));
    }
}