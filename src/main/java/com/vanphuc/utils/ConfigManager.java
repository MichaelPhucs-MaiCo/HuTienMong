package com.vanphuc.utils;

import com.google.gson.*;
import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.Window;
import com.vanphuc.gui.navigation.*;
import com.vanphuc.module.Module;
import com.vanphuc.module.Modules;
import com.vanphuc.module.settings.Setting; // Đã loại bỏ import thừa, chỉ giữ lại class cha Setting
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("hutienmong.json").toFile();

    public static void save() {
        JsonObject root = new JsonObject();

        // --- LƯU CONFIG HỆ THỐNG (CLIENT SETTINGS) ---
        JsonObject clientObj = new JsonObject();
        clientObj.addProperty("snapping", ClientConfig.snapping);
        clientObj.addProperty("showGrid", ClientConfig.showGrid);
        clientObj.addProperty("gridSize", ClientConfig.gridSize);
        clientObj.addProperty("guiKey", ClientConfig.guiKey);
        clientObj.addProperty("guiMods", ClientConfig.guiMods);
        clientObj.addProperty("blockKeybindInGui", ClientConfig.blockKeybindInGui);

        // Lưu trạng thái của Modules Manager
        clientObj.addProperty("modulesManagerX", ToggleModulesList.savedX);
        clientObj.addProperty("modulesManagerY", ToggleModulesList.savedY);
        clientObj.addProperty("modulesManagerExpanded", ToggleModulesList.isExpanded);

        root.add("client", clientObj);

        // --- LƯU MODULE VÀ SETTINGS ---
        JsonObject modulesObj = new JsonObject();
        for (Module module : Modules.get().getAll()) {
            JsonObject moduleObj = new JsonObject();
            moduleObj.addProperty("active", module.isActive());

            // Lưu tọa độ Window của Module
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
                // Tối ưu hóa: Gọi hàm save() đa hình
                setting.save(settingsObj);
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
        for (String f : FriendManager.getFriends()) {
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

            // --- TẢI CONFIG HỆ THỐNG ---
            if (root.has("client")) {
                JsonObject clientObj = root.getAsJsonObject("client");
                if (clientObj.has("snapping")) ClientConfig.snapping = clientObj.get("snapping").getAsBoolean();
                if (clientObj.has("showGrid")) ClientConfig.showGrid = clientObj.get("showGrid").getAsBoolean();
                if (clientObj.has("gridSize")) ClientConfig.gridSize = clientObj.get("gridSize").getAsInt();
                if (clientObj.has("guiKey")) ClientConfig.guiKey = clientObj.get("guiKey").getAsInt();
                if (clientObj.has("guiMods")) ClientConfig.guiMods = clientObj.get("guiMods").getAsInt();
                if (clientObj.has("blockKeybindInGui")) ClientConfig.blockKeybindInGui = clientObj.get("blockKeybindInGui").getAsBoolean();

                // Load trạng thái của Modules Manager
                if (clientObj.has("modulesManagerX")) ToggleModulesList.savedX = clientObj.get("modulesManagerX").getAsFloat();
                if (clientObj.has("modulesManagerY")) ToggleModulesList.savedY = clientObj.get("modulesManagerY").getAsFloat();
                if (clientObj.has("modulesManagerExpanded")) ToggleModulesList.isExpanded = clientObj.get("modulesManagerExpanded").getAsBoolean();

                // Đồng bộ lại UI Settings trong ConfigPage
                ConfigPage.snappingSetting.setValue(ClientConfig.snapping);
                ConfigPage.showGridSetting.setValue(ClientConfig.showGrid);
                ConfigPage.gridSizeSetting.setValue((double) ClientConfig.gridSize);
                ConfigPage.guiKeybindSetting.setKey(ClientConfig.guiKey, ClientConfig.guiMods);
            }

            // --- TẢI MODULE VÀ SETTINGS ---
            if (root.has("modules")) {
                JsonObject modulesObj = root.getAsJsonObject("modules");

                for (Module module : Modules.get().getAll()) {
                    if (modulesObj.has(module.name)) {
                        JsonObject moduleObj = modulesObj.getAsJsonObject(module.name);

                        // Load trạng thái Active
                        if (moduleObj.has("active") && moduleObj.get("active").getAsBoolean()) {
                            if (!module.isActive()) module.toggle();
                        } else {
                            if(module.isActive()) module.toggle();
                        }

                        // Load tọa độ Window của Module
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

                        // Load Settings
                        if (moduleObj.has("settings")) {
                            JsonObject settingsObj = moduleObj.getAsJsonObject("settings");
                            for (Setting<?> setting : module.getSettings()) {
                                if (settingsObj.has(setting.getName())) {
                                    try {
                                        // Tối ưu hóa: Gọi hàm load() đa hình
                                        setting.load(settingsObj);
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
                FriendManager.setFriends(loadedFriends);
            }

            // --- TẢI TRẠNG THÁI & TỌA ĐỘ CỦA HUD ---
            if (root.has("huds") && GuiManager.getInstance().pages != null) {
                JsonObject hudsObj = root.getAsJsonObject("huds");
                for (Page page : GuiManager.getInstance().pages) {
                    for (Window window : page.windows) {
                        if (window instanceof HudWindow hw) {
                            if (hudsObj.has(hw.getTitle())) {
                                JsonObject hudJson = hudsObj.getAsJsonObject(hw.getTitle());
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