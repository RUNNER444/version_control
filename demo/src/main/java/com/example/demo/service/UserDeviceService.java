package com.example.demo.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import com.example.demo.model.UserDevice;
import com.example.demo.repository.UserDeviceRepository;
import com.example.demo.specifications.UserDeviceSpecifications;

@Service
@org.springframework.transaction.annotation.Transactional (readOnly = true)
public class UserDeviceService {
    private final UserDeviceRepository userDeviceRepository;
    
    public UserDeviceService(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }
    
    @PostConstruct
    public void init() {

    }

    @Cacheable (value = "userDevices", key = "#root.Methodname")
    public List<UserDevice> getAll() {
        return userDeviceRepository.findAll();
    }

    public List <UserDevice> getAllByUserId (Long id) {
        return userDeviceRepository.findAllByUserId(id);
    }

    @CacheEvict (value = "userDevices", allEntries = true)
    @Transactional
    public UserDevice create (UserDevice userDevice) {
        return userDeviceRepository.save(userDevice);
    }

    @Cacheable (value = "userDevices", key = "#id")
    public UserDevice getById(Long id) {
        return userDeviceRepository.findById(id).orElse(null);
    }

    @Caching (evict = {
        @CacheEvict(value = "userDevices", allEntries = true),
        @CacheEvict(value = {"userDevices", "userDevice"}, key = "#id")
    })
    @Transactional
    public UserDevice update(Long id, UserDevice userDevice) {
        return userDeviceRepository.findById(id).map(existingUserDevice -> {
            existingUserDevice.setUserId(userDevice.getUserId());
            existingUserDevice.setPlatform(userDevice.getPlatform());
            existingUserDevice.setCurrentVersion(userDevice.getCurrentVersion());
            existingUserDevice.setLastSeen(userDevice.getLastSeen());
            return userDeviceRepository.save(existingUserDevice);
        }).orElse(null);
    }

    @Caching (evict = {
        @CacheEvict(value = "userDevices", allEntries = true),
        @CacheEvict(value = {"userDevices", "userDevice"}, key = "#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        if (userDeviceRepository.existsById(id)) {
            userDeviceRepository.deleteById(id);
            return true;
        }
        else {
            return false;
        }
    }

    public Page<UserDevice> getByFilter (Long userId, String version, Pageable pageable) {
        return userDeviceRepository.findAll(UserDeviceSpecifications.filter(userId, version), pageable);
    }
}
