package com.github.wisemann64.ngabdungeons.mobs;

public class MobFlag<T> {

    private EnumMobFlags key;
    private T value;

    public MobFlag(EnumMobFlags key, T data) {
        this.key = key;
        value = data;
    }

    public EnumMobFlags getKey() {
        return key;
    }

    public void setKey(EnumMobFlags key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
