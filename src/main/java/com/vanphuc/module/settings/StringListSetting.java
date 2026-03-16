package com.vanphuc.module.settings;

import java.util.List;

public class StringListSetting extends Setting<List<String>> {
    public StringListSetting(String name, List<String> defaultValue) {
        super(name, defaultValue);
    }
}