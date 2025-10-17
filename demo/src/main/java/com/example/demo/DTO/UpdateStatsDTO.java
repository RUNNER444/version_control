package com.example.demo.DTO;

import java.util.Map;

import com.example.demo.enums.PlatformType;

public record UpdateStatsDto (String version,
Map<PlatformType, Integer> usersCount,
Double globalUpdateRate) {
    
}
