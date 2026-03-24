package com.vanphuc.module.settings;

import com.google.gson.JsonObject;

/**
 * A setting that represents a boolean toggle.
 */
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public void toggle() {
        setValue(!getValue());
    }

    public boolean isEnabled() {
        return getValue();
    }

    @Override
    public void save(JsonObject parent) {
        parent.addProperty(getName(), getValue());
    }

    @Override
    public void load(JsonObject parent) {
        if (parent.has(getName())) {
            setValue(parent.get(getName()).getAsBoolean());
        }
    }
}