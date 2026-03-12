package com.vanphuc.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public abstract class Module {
    public final String name;
    public final String description;
    public final Category category;
    protected final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean active;

    public Module(Category category, String name, String description) {
        this.category = category;
        this.name = name;
        this.description = description;
    }

    public void toggle() {
        if (active) onDeactivate();
        else onActivate();
    }

    public void onActivate() {
        active = true;
        info("Module " + name + " đã bật.");
    }

    public void onDeactivate() {
        active = false;
        info("Module " + name + " đã tắt.");
    }

    public boolean isActive() {
        return active;
    }

    protected void info(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.of("§7[§bHutienMong§7] §f" + message), false);
        }
    }
}