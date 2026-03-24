package com.vanphuc.module.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class StringListSetting extends Setting<List<String>> {
    public StringListSetting(String name, List<String> defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public void save(JsonObject parent) {
        JsonArray array = new JsonArray();
        for (String s : getValue()) {
            array.add(s);
        }
        parent.add(getName(), array);
    }

    @Override
    public void load(JsonObject parent) {
        if (parent.has(getName()) && parent.get(getName()).isJsonArray()) {
            JsonArray array = parent.getAsJsonArray(getName());
            List<String> list = new ArrayList<>();
            for (JsonElement element : array) {
                list.add(element.getAsString());
            }
            setValue(list);
        }
    }
}