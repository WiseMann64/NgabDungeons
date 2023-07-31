package com.github.wisemann64.ngabdungeons.utils;

import java.util.EnumMap;

public class EnumDoubleMap<T extends Enum<T>> {

    private final EnumMap<T,Double> map;

    public EnumDoubleMap(Class<T> keyType) {
        map = new EnumMap<>(keyType);
    }

    public double get(T key) {
        return map.getOrDefault(key,0d);
    }

    public void add(T key, Double value) {
        if (value != null) map.put(key,get(key)+value);
    }
    public void add(T key, Float value) {
        if (value != null) map.put(key,get(key)+value);
    }
    public void add(T key, Integer value) {
        if (value != null) map.put(key,get(key)+value);
    }
    public void add(T key, Long value) {
        if (value != null) map.put(key,get(key)+value);
    }

    public void putAll(EnumDoubleMap<T> map) {
        this.map.putAll(map.map);
    }

    public void put(T key, double value) {
        map.put(key,value);
    }

    public EnumMap<T, Double> getMap() {
        return map;
    }

    public Number getOrDefault(T stats, double v) {
        return map.getOrDefault(stats,v);
    }
}
