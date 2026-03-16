package com.vanphuc.utils;

public class HudConfig {
    public String name;
    public float x;
    public float y;
    public boolean enabled;

    public HudConfig(String name, float x, float y, boolean enabled) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.enabled = enabled;
    }
}