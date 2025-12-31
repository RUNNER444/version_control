package com.example.demo.repository.projection;

import com.example.demo.enums.PlatformType;

public interface VersionDistribution {
    String getVersion();
    PlatformType getPlatform();
    Long getUsersCount();
}
