package com.vanphuc.utils;

import com.google.gson.*;
import com.vanphuc.module.Module;
import com.vanphuc.module.Modules;
import com.vanphuc.module.settings.BooleanSetting;
import com.vanphuc.module.settings.KeybindSetting;
import com.vanphuc.module.settings.ModeSetting;
import com.vanphuc.module.settings.NumberSetting;
import com.vanphuc.module.settings.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

public class ConfigManager {
    // Dùng GsonBuilder để xuất file JSON có thụt lề cho đẹp mắt, dễ đọc
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    // Lưu thẳng file hutienmong.json vào thư mục config của Minecraft
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

                    // Load trạng thái Bật/Tắt của Module
                    if (moduleObj.has("active") && moduleObj.get("active").getAsBoolean()) {
                        if (!module.isActive()) module.toggle(); // Bật module lên nếu file json ghi là true
                    }

                    // Load Settings của Module
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
                                        int key = keyObj.get("key").getAsInt();
                                        int mods = keyObj.get("mods").getAsInt();
                                        ks.setKey(key, mods);
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