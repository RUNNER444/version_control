package com.example.demo.model;

import java.time.LocalDateTime;

import com.example.demo.enums.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(indexes = @Index(name = "idx_app_version", columnList = "version", unique = true))
public class AppVersion {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100, unique = true)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PlatformType platform;

    @Column(nullable = false)
    private LocalDateTime releaseDate;

    @Column(columnDefinition = "TEXT")
    private String changelog;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UpdateType updateType;
    
    private boolean active;
}
