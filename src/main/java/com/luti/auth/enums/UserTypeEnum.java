package com.luti.auth.enums;

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
}
