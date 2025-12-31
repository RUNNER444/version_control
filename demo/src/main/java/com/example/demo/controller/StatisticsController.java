package com.example.demo.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.StatisticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
@Tag(name = "Statistics", description = "Shows statistics on users switching to new app versions")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @Operation (
    summary = "Generate heatmap of app version distribution on platforms",
    description = "Creates a heatmap showing the number of users on specified app versions and platforms in html file")
    @GetMapping("/heatmap")
    public ResponseEntity <Resource> getHeatmap () {
        Resource resource = statisticsService.generateHeatmap();
        return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=heatmap.html")
        .contentType(MediaType.TEXT_HTML)
        .body(resource);
    }
}
