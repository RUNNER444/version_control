package com.example.demo.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.example.demo.model.UserDevice;

public class UserDeviceSpecifications {
    private static Specification<UserDevice> userIdLike (Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("userId"), userId);
        };
    }

    private static Specification<UserDevice> currentVersionLike (String version) {
        return (root, query, criteriaBuilder) -> {
            if (version == null || version.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like (
                criteriaBuilder.lower(root.get("currentVersion")),
                "%" + version.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<UserDevice> filter (Long userId, String version) {
        return Specification.allOf(userIdLike(userId), currentVersionLike(version));
    }
}
