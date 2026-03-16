package com.vanphuc.module;

import com.vanphuc.module.settings.Setting;
import com.vanphuc.module.settings.KeybindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final String description;
    public final ItemStack icon;
    protected final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean active;
    private final List<Setting<?>> settings = new ArrayList<>();
    public final KeybindSetting keybind = new KeybindSetting("Keybind", -1, this);
    public void onUpdate() {}

    public Module(String name, String description, ItemStack icon) {
        this.name = name;
        this.description = description;
        this.icon = icon != null ? icon : Items.AIR.getDefaultStack();
        this.addSetting(keybind);
    }

    public Module(String name, String description) {
        this(name, description, Items.AIR.getDefaultStack());
    }

    public void toggle() {
        if (active)
            onDeactivate();
        else
            onActivate();
    }

    public void onActivate() {
        active = true;
        info("Module " + name + " đã bật.");
    }

    public void onDeactivate() {
        active = false;
        info("Module " + name + " đã tắt.");
    }



    public void addSetting(Setting<?> setting) {
        this.settings.add(setting);
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public boolean isActive() {
        return active;
    }

    protected void info(String message) {
        // Gọi qua ChatUtils để có format xịn sò hơn
        com.vanphuc.utils.ChatUtils.info(this, message);
    }

    // Cậu cũng có thể thêm hàm error để dùng trong module:
    protected void error(String message) {
        com.vanphuc.utils.ChatUtils.error(this, message);
    }
}