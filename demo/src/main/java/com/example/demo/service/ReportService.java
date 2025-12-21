package com.example.demo.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.example.demo.dto.UserDeviceResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final UserDeviceService userDeviceService;

    @Value("${report.template-location}")
    private Resource templateLocation;

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    public ByteArrayResource getOutdatedDevicesReport (Long userId, String platform) {
        List <UserDeviceResponseDto> devices = userDeviceService.getOutdatedDevices(userId, platform);
        Map <String, Object> data = new HashMap<>();
        data.put("devices", devices);

        try (InputStream is = templateLocation.getInputStream()) {
            byte[] reportBytes = JxlsPoiTemplateFillerBuilder.newInstance().withTemplate(is).buildAndFill(data);
            logger.info("Successfully generated report of outdated devices for user {}", userId);
            return new ByteArrayResource(reportBytes);
        }
        catch (Exception e) {
            throw new RuntimeException("Error while creating the report", e);
        }    
    }
}
