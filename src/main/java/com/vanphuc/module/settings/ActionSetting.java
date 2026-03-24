package com.vanphuc.module.settings;

import com.google.gson.JsonObject;

public class ActionSetting extends Setting<Runnable> {
    public ActionSetting(String name, Runnable action) {
        super(name, action);
    }

    public void execute() {
        getValue().run();
    }

    @Override
    public void save(JsonObject parent) {
        // No implementation needed for ActionSetting
    }

    @Override
    public void load(JsonObject parent) {
        // No implementation needed for ActionSetting
    }
}