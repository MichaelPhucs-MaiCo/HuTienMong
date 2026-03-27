package com.vanphuc.module.settings;

import com.google.gson.JsonObject;

/**
 * Abstract class representing a setting for the Minecraft client.
 * * @param <T> The type of the value stored in the setting.
 */
public abstract class Setting<T> {
    private final String name;
    private T value;
    private final T defaultValue;

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public boolean isVisible() {
        return true;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void reset() {
        this.value = defaultValue;
    }

    /**
     * Tự lưu giá trị của setting này vào JsonObject của Module.
     * * @param parent Đối tượng JSON của module chứa setting này.
     */
    public abstract void save(JsonObject parent);

    /**
     * Tự nạp giá trị từ JsonObject vào setting này.
     * * @param parent Đối tượng JSON của module chứa setting này.
     */
    public abstract void load(JsonObject parent);
}