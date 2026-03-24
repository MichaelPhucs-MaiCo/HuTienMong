package com.vanphuc.module.settings;

import com.google.gson.JsonObject;

public class EnumSetting<T extends Enum<T>> extends Setting<T> {
    private final T[] values;

    public EnumSetting(String name, T defaultValue) {
        super(name, defaultValue);
        this.values = defaultValue.getDeclaringClass().getEnumConstants();
    }

    public T[] getValues() {
        return values;
    }

    @Override
    public void save(JsonObject parent) {
        parent.addProperty(getName(), getValue().name());
    }

    @Override
    public void load(JsonObject parent) {
        if (parent.has(getName())) {
            String enumName = parent.get(getName()).getAsString();
            for (T enumValue : getValues()) {
                if (enumValue.name().equalsIgnoreCase(enumName)) {
                    setValue(enumValue);
                    break;
                }
            }
        }
    }
}