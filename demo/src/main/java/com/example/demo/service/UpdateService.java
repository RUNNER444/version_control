package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.dto.AppVersionResponseDto;
import com.example.demo.dto.UpdateResponseDto;
import com.example.demo.dto.UserDeviceResponseDto;
import com.example.demo.enums.PlatformType;
import com.example.demo.enums.UpdateType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UpdateService {
    private final AppVersionService appVersionService;
    private final UserDeviceService userDeviceService;
    private static final Logger logger = LoggerFactory.getLogger(UpdateService.class);

    public UpdateResponseDto checkUserDeviceForUpdate (Long id) {
        UserDeviceResponseDto userDevice = userDeviceService.getById(id);
        String deviceVersion = userDevice.currentVersion();
        PlatformType devicePlatform = userDevice.platform();

        AppVersionResponseDto latestVersion = appVersionService.getLatestVersion(String.valueOf(devicePlatform));
        AppVersionResponseDto currentVersion = appVersionService.getByVersionAndPlatform(deviceVersion, devicePlatform);

        if (currentVersion.updateType() == UpdateType.OPTIONAL) {
            logger.info("Device {} has an optional update to app version: {}", id, latestVersion.version());
            return new UpdateResponseDto(id, userDevice.userId(), true, deviceVersion, latestVersion.version(), UpdateType.OPTIONAL);
        }

        if (currentVersion.updateType() == UpdateType.MANDATORY) {
            if (currentVersion.active()) logger.info("For proper work device {} is required to be updated to version: {}", id, latestVersion.version());
            else logger.info("Device {} has an app version that's no longer supported!", id);
            return new UpdateResponseDto(id, userDevice.userId(), true, deviceVersion, latestVersion.version(), UpdateType.MANDATORY);
        }

        if (currentVersion.updateType() == UpdateType.DEPRECATED) {
            if (currentVersion.active()) logger.info("Device {} has a deprecated app version. Need an update!", id);
            else logger.info(" Device {} has a deprecated app version that's no longer supported!", id);
            return new UpdateResponseDto(id, userDevice.userId(), true, deviceVersion, latestVersion.version(), UpdateType.DEPRECATED);
        }

        logger.info("Device {} is up to date and has the latest release", id);
        return new UpdateResponseDto(id, userDevice.userId(), false, deviceVersion, deviceVersion, UpdateType.UNAVAILABLE);
    }

    public List<UpdateResponseDto> getDevicesWithUpdateType (String type) {
        UpdateType updateType;
        try {updateType = UpdateType.valueOf(type.toUpperCase());}
        catch (IllegalArgumentException e) {throw new IllegalArgumentException("Invalid update type is provided. Must be one of "+
            "UNAVAILABLE, OPTIONAL, MANDATORY, DEPRECATED.");}

        List <UserDeviceResponseDto> allDevices = userDeviceService.getAll();
        List <UpdateResponseDto> devicesWithSpecifiedUpdateType = new ArrayList<>();
        for (UserDeviceResponseDto userDevice : allDevices) {
            UpdateResponseDto updateData = checkUserDeviceForUpdate(userDevice.id());
            if (updateData.updateType() == updateType) devicesWithSpecifiedUpdateType.add(updateData);
        }
        logger.info("Found {} devices with {} update type", devicesWithSpecifiedUpdateType.size(), type);

        return devicesWithSpecifiedUpdateType;
    }

    public UpdateResponseDto updateDevice (Long id) {
        UpdateResponseDto updateData = checkUserDeviceForUpdate(id);
        if (updateData.updateType() == UpdateType.UNAVAILABLE) {
            return new UpdateResponseDto(id, updateData.userId(), false, updateData.currentVersion(), updateData.currentVersion(), UpdateType.UNAVAILABLE); 
        }

        userDeviceService.updateOnlyVersion(id, updateData.latestVersion());
        logger.info("Successfully updated user device {} to the latest version: {}", id, updateData.latestVersion());
        return new UpdateResponseDto(id, updateData.userId(), false, updateData.latestVersion(), updateData.latestVersion(), UpdateType.UNAVAILABLE);
    }

    public List<UpdateResponseDto> forceUpdateAllOutdated () {
        int successCount = 0;
        List <UpdateResponseDto> response = new ArrayList<>();
        List <UpdateResponseDto> outdatedDevices = getDevicesWithUpdateType("MANDATORY");
        outdatedDevices.addAll(getDevicesWithUpdateType("DEPRECATED"));

        for (UpdateResponseDto updateData : outdatedDevices) {
            try {response.add(updateDevice(updateData.userDeviceId())); successCount++;}
            catch (Exception e) {
                logger.warn("Failed to update device {}", updateData.userDeviceId());
            }
        }
        logger.info("Updated {} devices to the last version", successCount);

        return response;
    }
}
