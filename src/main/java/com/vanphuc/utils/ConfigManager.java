package com.vanphuc.utils;

import com.google.gson.*;
import com.vanphuc.module.Module;
import com.vanphuc.module.Modules;
import com.vanphuc.module.settings.*;
// NHỚ THÊM IMPORT NÀY:
import com.vanphuc.module.settings.StringListSetting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("hutienmong.json").toFile();

    public static void save() {
        JsonObject root = new JsonObject();
        JsonObject modulesObj = new JsonObject();

        for (Module module : Modules.get().getAll()) {
            JsonObject moduleObj = new JsonObject();
            moduleObj.addProperty("active", module.isActive());

            JsonObject settingsObj = new JsonObject();
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof BooleanSetting bs) {
                    settingsObj.addProperty(setting.getName(), bs.getValue());
                } else if (setting instanceof NumberSetting ns) {
                    settingsObj.addProperty(setting.getName(), ns.getValue());
                } else if (setting instanceof ModeSetting ms) {
                    settingsObj.addProperty(setting.getName(), ms.getValue());
                } else if (setting instanceof KeybindSetting ks) {
                    JsonObject keyObj = new JsonObject();
                    keyObj.addProperty("key", ks.getKey());
                    keyObj.addProperty("mods", ks.getModifiers());
                    settingsObj.add(setting.getName(), keyObj);
                }
                // THÊM ĐOẠN NÀY ĐỂ LƯU LIST XUỐNG JSON 👇
                else if (setting instanceof StringListSetting sls) {
                    JsonArray arr = new JsonArray();
                    for (String s : sls.getValue()) {
                        arr.add(s);
                    }
                    settingsObj.add(setting.getName(), arr);
                }
            }
            moduleObj.add("settings", settingsObj);
            modulesObj.add(module.name, moduleObj);
        }
        root.add("modules", modulesObj);

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            System.out.println("[Hư Tiên Mộng] Lỗi khi lưu config: " + e.getMessage());
        }
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) return;

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (!root.has("modules")) return;

            JsonObject modulesObj = root.getAsJsonObject("modules");

            for (Module module : Modules.get().getAll()) {
                if (modulesObj.has(module.name)) {
                    JsonObject moduleObj = modulesObj.getAsJsonObject(module.name);

                    if (moduleObj.has("active") && moduleObj.get("active").getAsBoolean()) {
                        if (!module.isActive()) module.toggle();
                    }

                    if (moduleObj.has("settings")) {
                        JsonObject settingsObj = moduleObj.getAsJsonObject("settings");
                        for (Setting<?> setting : module.getSettings()) {
                            if (settingsObj.has(setting.getName())) {
                                try {
                                    if (setting instanceof BooleanSetting bs) {
                                        bs.setValue(settingsObj.get(setting.getName()).getAsBoolean());
                                    } else if (setting instanceof NumberSetting ns) {
                                        ns.setValue(settingsObj.get(setting.getName()).getAsDouble());
                                    } else if (setting instanceof ModeSetting ms) {
                                        ms.setValue(settingsObj.get(setting.getName()).getAsString());
                                    } else if (setting instanceof KeybindSetting ks) {
                                        JsonObject keyObj = settingsObj.getAsJsonObject(setting.getName());
                                        ks.setKey(keyObj.get("key").getAsInt(), keyObj.get("mods").getAsInt());
                                    }
                                    // THÊM ĐOẠN NÀY ĐỂ ĐỌC LIST TỪ JSON LÊN 👇
                                    else if (setting instanceof StringListSetting sls) {
                                        JsonArray arr = settingsObj.getAsJsonArray(setting.getName());
                                        List<String> list = new ArrayList<>();
                                        for (JsonElement e : arr) {
                                            list.add(e.getAsString());
                                        }
                                        sls.setValue(list);
                                    }
                                } catch (Exception e) {
                                    System.out.println("[Hư Tiên Mộng] Lỗi load setting '" + setting.getName() + "' của module " + module.name);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            System.out.println("[Hư Tiên Mộng] Lỗi khi đọc config: " + e.getMessage());
        }
    }
}