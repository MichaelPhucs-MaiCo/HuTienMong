package com.vanphuc.module.settings;

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
}
