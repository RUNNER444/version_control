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
        logger.info("Attempting to check if User Device {} needs an update", id);

        try {
            UserDeviceResponseDto userDevice = userDeviceService.getById(id);
            String deviceVersion = userDevice.currentVersion();
            PlatformType devicePlatform = userDevice.platform();
            logger.debug("Device {} - Version: {} - Platform: {}", id, deviceVersion, devicePlatform);

            AppVersionResponseDto latestVersion = appVersionService.getLatestVersion(String.valueOf(devicePlatform));

            AppVersionResponseDto currentVersion = appVersionService.getByVersionAndPlatform(deviceVersion, devicePlatform);

            if (currentVersion.updateType() == UpdateType.OPTIONAL) {
                logger.info("Device {} has an optional update to app version: {}", id, latestVersion.version());
                return new UpdateResponseDto(id, true, deviceVersion, latestVersion.version(), UpdateType.OPTIONAL);
            }

            if (currentVersion.updateType() == UpdateType.MANDATORY) {
                if (currentVersion.active()) logger.info("For proper work device {} is required to be updated to version: {}", id, latestVersion.version());
                else logger.info("IMMEDIATELY UPDATE! Device {} has an app version that's no longer supported!", id);
                return new UpdateResponseDto(id, true, deviceVersion, latestVersion.version(), UpdateType.MANDATORY);
            }

            if (currentVersion.updateType() == UpdateType.DEPRECATED) {
                if (currentVersion.active()) logger.info("Device {} has a deprecated app version. Need an update!", id);
                else logger.info("IMMEDIATELY UPDATE! Device {} has a deprecated app version that's no longer supported!", id);
                return new UpdateResponseDto(id, true, deviceVersion, latestVersion.version(), UpdateType.DEPRECATED);
            }

            logger.info("Device {} is up to date and has the latest release", id);
            return new UpdateResponseDto(id, false, deviceVersion, deviceVersion, UpdateType.UNAVAILABLE);
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid id is provided. Must be a number. Error: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error occured while checking device for update. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<UpdateResponseDto> getDevicesWithUpdateType (String type) {
        logger.info("Finding devices with version that has {} update type..", type);

        try {
            logger.debug("Converting update type string '{}' to UpdateType enum", type);
            UpdateType updateType = UpdateType.valueOf(type.toUpperCase());

            List <UserDeviceResponseDto> allDevices = userDeviceService.getAll();

            List <UpdateResponseDto> devicesWithSpecifiedUpdateType = new ArrayList<>();
            for (UserDeviceResponseDto userDevice : allDevices) {
                UpdateResponseDto updateData = checkUserDeviceForUpdate(userDevice.id());
                if (updateData.updateType() == updateType) devicesWithSpecifiedUpdateType.add(updateData);
            }
            logger.info("Found {} devices with {} update type", devicesWithSpecifiedUpdateType.size(), type);

            return devicesWithSpecifiedUpdateType;
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid update type is provided. Must be one of UNAVAILABLE, OPTIONAL, "+
            "MANDATORY, DEPRECATED. Error: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error occured while getting devices with specified update type {}. Error: {}", type, e.getMessage(), e);
            throw e;
        }
    }

    public UpdateResponseDto updateDevice (Long id) {
        logger.info("Trying to update user device with id: {}", id);

        try {
            logger.debug("Checking the possibility of update installation on user device..");
            UpdateResponseDto updateData = checkUserDeviceForUpdate(id);

            if (updateData.updateType() == UpdateType.UNAVAILABLE) {
                return new UpdateResponseDto(id, false, updateData.currentVersion(), updateData.currentVersion(), UpdateType.UNAVAILABLE); 
            }

            logger.debug("Updating the version of user device to the latest available one");
            userDeviceService.updateOnlyVersion(id, updateData.latestVersion());

            logger.info("Successfully updated user device {} to the latest version: {}", id, updateData.latestVersion());
            return new UpdateResponseDto(id, false, updateData.latestVersion(), updateData.latestVersion(), UpdateType.UNAVAILABLE);
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid id is provided. Must be a number. Error: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error occured while updating device {}. Error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public List<UpdateResponseDto> forceUpdateAllOutdated () {
        logger.info("Trying to force update all user devices that which versions are outdated (MANDATORY OR DEPRECATED)..");

        List <UpdateResponseDto> response = new ArrayList<>();

        try {
            logger.debug("Getting all devices with outdated versions");
            List <UpdateResponseDto> outdatedDevices = getDevicesWithUpdateType("MANDATORY");
            outdatedDevices.addAll(getDevicesWithUpdateType("DEPRECATED"));

            logger.debug("Updating in total {} user devices", outdatedDevices.size());
            for (UpdateResponseDto updateData : outdatedDevices) {
                response.add(updateDevice(updateData.userDeviceId()));
            }
            logger.info("Updated {} devices to the last version", response.size());

            return response;
        }
        catch (Exception e) {
            logger.info("Updated {} devices to the last version", response.size());
            logger.error("Unexpected error occured while force updating devices. Error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
