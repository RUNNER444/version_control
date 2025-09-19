package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AppVersion {
    private String version;
    private Platform platform;
    private LocalDateTime releaseDate;
    private String changelog;
    private UpdateType updateType;
    private boolean isActive;
}
