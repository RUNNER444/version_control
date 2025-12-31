package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.demo.dto.UpdateStatsDto;
import com.example.demo.enums.PlatformType;
import com.example.demo.repository.UserDeviceRepository;
import com.example.demo.repository.projection.VersionDistribution;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {
    private final UserDeviceRepository userDeviceRepository;
    private final TemplateEngine templateEngine;
    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    public ByteArrayResource generateHeatmap() {
        List <VersionDistribution> rawStats = userDeviceRepository.getVersionDistrib();
        Map <String, Map <PlatformType, Long>> grouped = rawStats.stream()
        .collect(Collectors.groupingBy(
            VersionDistribution :: getVersion,
            Collectors.toMap(VersionDistribution :: getPlatform, VersionDistribution :: getUsersCount)
        ));

        List <String> versions = grouped.keySet().stream().sorted().toList();
        List <String> platforms = List.of(PlatformType.ANDROID.name(), PlatformType.IOS.name());
        List <List <Object>> chartData = new ArrayList<>();

        try {
            for (int v = 0; v < versions.size(); v++) {
                Map <PlatformType, Long> platformCount = grouped.get(versions.get(v));
                for (int p = 0; p < platforms.size(); p++) {
                    long count = platformCount.getOrDefault(PlatformType.valueOf(platforms.get(p)), 0l);
                    chartData.add(List.of(v, p, count));
                }
            }

            Context context = new Context();
            context.setVariable("heatmapData", new UpdateStatsDto(versions, platforms, chartData));

            logger.info("Successfully generated heatmap of version distribution");
            return new ByteArrayResource(templateEngine.process("heatmap-template", context).getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            throw new RuntimeException("Error while generating the heatmap", e);
        }
    }
}
