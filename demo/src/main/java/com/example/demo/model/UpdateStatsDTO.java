package com.example.demo.model;

import java.util.Map;

public class UpdateStatsDTO {
    private String version;
    private Map<Platform, Integer> usersCount;
    private Double globalUpdateRate;
}
