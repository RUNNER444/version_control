package com.example.demo.dto;

import java.util.List;

public record UpdateStatsDto (
    List <String> versions,
    List <String> platforms,
    List <List <Object>> data
) {
    
}
