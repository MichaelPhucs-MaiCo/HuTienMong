package com.vanphuc.module.settings;

public class EnumSetting<T extends Enum<T>> extends Setting<T> {
    private final T[] values;

    public EnumSetting(String name, T defaultValue) {
        super(name, defaultValue);
        this.values = defaultValue.getDeclaringClass().getEnumConstants();
    }

    public T[] getValues() {
        return values;
    }
}