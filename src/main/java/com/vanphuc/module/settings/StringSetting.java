package com.vanphuc.module.settings;

import com.google.gson.JsonObject;

public class StringSetting extends Setting<String> {
    public StringSetting(String name, String defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public void save(JsonObject parent) {
        parent.addProperty(getName(), getValue());
    }

    @Override
    public void load(JsonObject parent) {
        if (parent.has(getName())) {
            setValue(parent.get(getName()).getAsString());
        }
    }
}