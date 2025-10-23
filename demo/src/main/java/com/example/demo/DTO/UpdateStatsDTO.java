package com.example.demo.dto;

import java.util.Map;

import com.example.demo.enums.PlatformType;

public record UpdateStatsDto (String version,
Map<PlatformType, Integer> usersCount,
Double globalUpdateRate) {
    
}
