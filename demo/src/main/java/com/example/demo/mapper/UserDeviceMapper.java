package com.example.demo.mapper;

import com.example.demo.dto.UserDeviceRequestDto;
import com.example.demo.dto.UserDeviceResponseDto;
import com.example.demo.model.UserDevice;

public class UserDeviceMapper {
    public static UserDeviceRequestDto userDeviceToUserDeviceRequestDto (UserDevice userDevice) {
        return new UserDeviceRequestDto(userDevice.getUserId(), userDevice.getPlatform(),
        userDevice.getCurrentVersion());
    }

    public static UserDeviceResponseDto userDeviceToUserDeviceResponseDto (UserDevice userDevice) {
        return new UserDeviceResponseDto(userDevice.getId(), userDevice.getUserId(),
        userDevice.getPlatform(), userDevice.getCurrentVersion(), userDevice.getLastSeen());
    }
}
