package com.luti.auth.enums;

import jakarta.annotation.PostConstruct;

public enum UserTypeEnum {
    USER(1L, "USER"),
    ADMIN(2L, "ADMIN");

    private final Long id;
    private final String name;

    UserTypeEnum(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }

    public static UserTypeEnum findById(Long id) {
        for (UserTypeEnum type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }

}
