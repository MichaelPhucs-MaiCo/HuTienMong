package com.vanphuc.module.settings;

/**
 * Abstract class representing a setting for the Minecraft client.
 * 
 * @param <T> The type of the value stored in the setting.
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
}
