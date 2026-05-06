package com.tomorrowmust.system.domain.Enum;

import lombok.Getter;

@Getter
public enum PermissionEnum {
    ADD("ADD", "Add Permission"),
    DELETE("DELETE", "Delete Permission"),
    UPDATE("UPDATE", "Update Permission"),
    VIEW("VIEW", "View Permission");

    private final String code;
    private final String description;

    PermissionEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PermissionEnum getByCode(String code) {
        for (PermissionEnum permission : PermissionEnum.values()) {
            if (permission.getCode().equals(code)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Invalid permission code: " + code);
    }
}
