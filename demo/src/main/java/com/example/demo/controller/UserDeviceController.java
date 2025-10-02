package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.UserDevice;
import com.example.demo.service.UserDeviceService;

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
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    UserDeviceController(UserDeviceService userDeviceService) {
        this.userDeviceService = userDeviceService;
    }

    @GetMapping("/userDevices")
    public List<UserDevice> getUserDevices() {
        return userDeviceService.getAll();
    }

    @GetMapping("/userDevices/{id}")
    public ResponseEntity<UserDevice> getUserDevice(@PathVariable Long id) {
        return ResponseEntity.ok().body(userDeviceService.getById(id));
    }
    

    @PostMapping("/userDevices")
    public ResponseEntity<UserDevice> addUserDevice(@RequestBody @Valid UserDevice userDevice) {
        UserDevice newUserDevice = userDeviceService.create(userDevice);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUserDevice);
    }
    
    @PutMapping("/userDevices/{id}")
    public ResponseEntity <UserDevice> editUserDevice(@PathVariable Long id, @RequestBody @Valid UserDevice userDevice) {   
        UserDevice updated = userDeviceService.update(id, userDevice);
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
}