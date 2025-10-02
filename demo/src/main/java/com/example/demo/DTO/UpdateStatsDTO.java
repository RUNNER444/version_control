package com.example.demo.DTO;

import java.util.Map;

import com.example.demo.enums.PlatformType;

public class UpdateStatsDTO {
    private String version;

    private Map<PlatformType, Integer> usersCount;
    
    private Double globalUpdateRate;
}
