package com.vanphuc.module.settings;

import java.util.List;

/**
 * A setting that allows selecting from a list of modes (Strings).
 */
public class ModeSetting extends Setting<String> {
    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String defaultValue, String... modes) {
        super(name, defaultValue);
        this.modes = List.of(modes);
        this.index = this.modes.indexOf(defaultValue);
        if (this.index == -1) {
            throw new IllegalArgumentException("Default value must be one of the modes");
        }
    }

    @Override
    public void setValue(String value) {
        int newIndex = modes.indexOf(value);
        if (newIndex != -1) {
            this.index = newIndex;
            super.setValue(value);
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        if (index >= 0 && index < modes.size()) {
            this.index = index;
            super.setValue(modes.get(index));
        }
    }

    public List<String> getModes() {
        return modes;
    }

    public void cycle() {
        index = (index + 1) % modes.size();
        super.setValue(modes.get(index));
    }
}
