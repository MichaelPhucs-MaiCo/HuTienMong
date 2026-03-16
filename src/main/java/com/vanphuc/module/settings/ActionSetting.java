package com.vanphuc.module.settings;

public class ActionSetting extends Setting<Runnable> {
    public ActionSetting(String name, Runnable action) {
        super(name, action);
    }
    public void execute() {
        getValue().run();
    }
}