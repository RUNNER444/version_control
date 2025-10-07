package com.example.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.demo.model.UserDevice;

public interface UserDeviceRepository extends 
    JpaRepository <UserDevice, Long>,
    JpaSpecificationExecutor <UserDevice> {
    List <UserDevice> findByUserId(Long title);
    List <UserDevice> findAllByUserId(Long id);
}
