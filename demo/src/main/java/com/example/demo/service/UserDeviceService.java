package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import com.example.demo.model.UserDevice;
import com.example.demo.repository.UserDeviceRepository;

@Service
public class UserDeviceService {
    private final UserDeviceRepository userDeviceRepository;
    
    public UserDeviceService(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }

    private List <UserDevice> userDevices = new ArrayList<>();
    
    @PostConstruct
    public void init() {

    }

    public List<UserDevice> getAll() {
        return userDeviceRepository.findAll();
    }

    public List <UserDevice> getAllByUserId (Long id) {
        return userDeviceRepository.findAllByUserId(id);
    }

    public UserDevice create (UserDevice userDevice) {
        return userDeviceRepository.save(userDevice);
    }

    public UserDevice getById(Long id) {
        for (UserDevice userDevice : userDevices) {
            if (userDevice.getId().equals(id)) {
                return userDeviceRepository.findById(id).orElse(null);
            }
        }
        return null;
    }

    public UserDevice update(Long id, UserDevice userDevice) {
        return userDeviceRepository.findById(id).map(existingUserDevice -> {
            existingUserDevice.setUserId(userDevice.getUserId());
            return userDeviceRepository.save(existingUserDevice);
        }).orElse(null);
    }

    public boolean deleteById(Long id) {
        if (userDeviceRepository.existsById(id)) {
            userDeviceRepository.deleteById(id);
            return true;
        }
        else {
            return false;
        }
    }
}
