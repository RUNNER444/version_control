package com.example.demo.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.example.demo.model.AppVersion;

public class AppVersionSpecifications {
    private static Specification<AppVersion> versionLike (String version) {
        return (root, query, criteriaBuilder) -> {
            if (version == null || version.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like (
                criteriaBuilder.lower(root.get("version")),
                "%" + version.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<AppVersion> filter (String version) {
        return Specification.allOf(versionLike(version));
    }
}
