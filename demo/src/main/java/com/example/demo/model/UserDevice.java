package com.example.demo.model;

import java.time.LocalDateTime;

import com.example.demo.enums.PlatformType;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class UserDevice {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private Long userId;

    private PlatformType platform;
    
    @NotBlank
    private String currentVersion;

    private LocalDateTime lastSeen;
}
