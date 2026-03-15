package com.vanphuc.utils.render;

import net.minecraft.client.MinecraftClient;

public class RenderUtils {
    public static double getMouseX() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.mouse.getX() * (double) mc.getWindow().getScaledWidth() / (double) mc.getWindow().getWidth();
    }

    public static double getMouseY() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.mouse.getY() * (double) mc.getWindow().getScaledHeight() / (double) mc.getWindow().getHeight();
    }
}
