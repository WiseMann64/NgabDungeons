package com.github.wisemann64.ngabdungeons.mobs;

import java.util.UUID;

public enum EnumMobFlags {
    BONE_BREAKER_HIT(UUID.class),
    REDUCE_DEFENSE(Double.class),

    ;
    private final Class<?> dataType;

    EnumMobFlags(Class<?> data) {
        dataType = data;
    }

    public Class<?> getDataType() {
        return dataType;
    }
}
