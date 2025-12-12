package com.example.demo.mapper;

import com.example.demo.dto.AppVersionResponseDto;
import com.example.demo.model.AppVersion;

public class AppVersionMapper {
    public static AppVersionResponseDto appVersionToAppVersionResponseDto (AppVersion appVersion) {
        return new AppVersionResponseDto(appVersion.getId(), appVersion.getVersion(),
        appVersion.getPlatform(), appVersion.getReleaseDate(), appVersion.getChangelog(),
        appVersion.getUpdateType(), appVersion.isActive());
    }
}
