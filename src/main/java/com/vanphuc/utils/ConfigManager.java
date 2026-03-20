package com.vanphuc.utils;

import com.google.gson.*;
import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.Window;
import com.vanphuc.gui.navigation.HudWindow;
import com.vanphuc.gui.navigation.Page;
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

            if (GuiManager.getInstance().pages != null && !GuiManager.getInstance().pages.isEmpty()) {
                for (Window window : GuiManager.getInstance().pages.get(0).windows) {
                    if (window instanceof com.vanphuc.gui.window.ModuleWindow mw && mw.getModule() == module) {
                        moduleObj.addProperty("showInGui", mw.showInGui);
                        moduleObj.addProperty("guiX", mw.getPosition().getX());
                        moduleObj.addProperty("guiY", mw.getPosition().getY());
                        break;
                    }
                }
            }

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
                else if (setting instanceof StringSetting strS) {
                    settingsObj.addProperty(setting.getName(), strS.getValue());
                }

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

        // --- LƯU TRẠNG THÁI & TỌA ĐỘ CỦA HUD ---
        JsonObject hudsObj = new JsonObject();
        if (GuiManager.getInstance().pages != null) {
            for (Page page : GuiManager.getInstance().pages) {
                for (Window window : page.windows) {
                    if (window instanceof HudWindow hw) {
                        JsonObject hudJson = new JsonObject();
                        // Đổi hw.position thành hw.getPosition()
                        hudJson.addProperty("x", hw.getPosition().getX());
                        hudJson.addProperty("y", hw.getPosition().getY());
                        hudJson.addProperty("enabled", hw.enabled);
                        hudsObj.add(hw.getTitle(), hudJson);
                    }
                }
            }
        }
        root.add("huds", hudsObj);

        // --- LƯU DANH SÁCH FRIEND ---
        JsonArray friendsArray = new JsonArray();
        for (String f : com.vanphuc.utils.FriendManager.getFriends()) {
            friendsArray.add(f);
        }
        root.add("friends", friendsArray);

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

            // --- TẢI MODULE VÀ SETTINGS ---
            if (root.has("modules")) {
                JsonObject modulesObj = root.getAsJsonObject("modules");

                for (Module module : Modules.get().getAll()) {
                    if (modulesObj.has(module.name)) {
                        JsonObject moduleObj = modulesObj.getAsJsonObject(module.name);

                        if (moduleObj.has("active") && moduleObj.get("active").getAsBoolean()) {
                            if (!module.isActive()) module.toggle();
                        }
                        if (GuiManager.getInstance().pages != null && !GuiManager.getInstance().pages.isEmpty()) {
                            for (Window window : GuiManager.getInstance().pages.get(0).windows) {
                                if (window instanceof com.vanphuc.gui.window.ModuleWindow mw && mw.getModule() == module) {
                                    if (moduleObj.has("showInGui")) mw.showInGui = moduleObj.get("showInGui").getAsBoolean();
                                    if (moduleObj.has("guiX")) mw.getPosition().setX(moduleObj.get("guiX").getAsFloat());
                                    if (moduleObj.has("guiY")) mw.getPosition().setY(moduleObj.get("guiY").getAsFloat());
                                    break;
                                }
                            }
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
                                        else if (setting instanceof StringSetting strS) {
                                            strS.setValue(settingsObj.get(setting.getName()).getAsString());
                                        }
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
            }

            // --- TẢI DANH SÁCH FRIEND ---
            if (root.has("friends")) {
                JsonArray friendsArray = root.getAsJsonArray("friends");
                List<String> loadedFriends = new ArrayList<>();
                for (JsonElement e : friendsArray) {
                    loadedFriends.add(e.getAsString());
                }
                com.vanphuc.utils.FriendManager.setFriends(loadedFriends);
            }

            // --- TẢI TRẠNG THÁI & TỌA ĐỘ CỦA HUD ---
            if (root.has("huds") && GuiManager.getInstance().pages != null) {
                JsonObject hudsObj = root.getAsJsonObject("huds");
                for (Page page : GuiManager.getInstance().pages) {
                    for (Window window : page.windows) {
                        if (window instanceof HudWindow hw) {
                            if (hudsObj.has(hw.getTitle())) {
                                JsonObject hudJson = hudsObj.getAsJsonObject(hw.getTitle());
                                // Đổi hw.position thành hw.getPosition()
                                if (hudJson.has("x")) hw.getPosition().setX(hudJson.get("x").getAsFloat());
                                if (hudJson.has("y")) hw.getPosition().setY(hudJson.get("y").getAsFloat());
                                if (hudJson.has("enabled")) hw.enabled = hudJson.get("enabled").getAsBoolean();
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