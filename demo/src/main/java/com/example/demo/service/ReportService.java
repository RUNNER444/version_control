package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.example.demo.dto.UserDeviceResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final UserDeviceService userDeviceService;
    private final SpringTemplateEngine springTemplateEngine;
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    public String getOutdatedDevicesReport (Long userId, String platform) {
        logger.debug("Generating report for outdated devices for user {}", userId);

        try {
            List <UserDeviceResponseDto> devices = userDeviceService.getOutdatedDevices(userId, platform);

            Context context = new Context();
            context.setVariable("devices", devices);
            context.setVariable("generatedDate", LocalDateTime.now().toString());
            logger.debug("Successfully generated context for report");

            return springTemplateEngine.process("outdated-report", context);
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid id or platform provided. Error: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error occured while forming report. Error: {}", e.getMessage(), e);
            throw e;
        }     
    }
}
