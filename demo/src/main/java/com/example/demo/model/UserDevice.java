package com.example.demo.model;

import java.time.LocalDateTime;

import com.example.demo.enums.PlatformType;

import jakarta.persistence.Column;
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

    @Column(nullable = false, length = 50)
    private Long userId;

    @Column(nullable = false, length = 50)
    private PlatformType platform;
    
    @NotBlank
    private String currentVersion;

    @Column(nullable = false)
    private LocalDateTime lastSeen;
}
