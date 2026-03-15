package com.vanphuc.gui.colors;

import net.minecraft.util.math.MathHelper;

public class Color {
    private float r, g, b, a;

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(float r, float g, float b) {
        this(r, g, b, 1.0f);
    }

    public Color(int rgb) {
        this.a = ((rgb >> 24) & 0xFF) / 255.0f;
        this.r = ((rgb >> 16) & 0xFF) / 255.0f;
        this.g = ((rgb >> 8) & 0xFF) / 255.0f;
        this.b = (rgb & 0xFF) / 255.0f;
    }

    public static Color fromHex(String hex) {
        if (hex.startsWith("#"))
            hex = hex.substring(1);
        int val = (int) Long.parseLong(hex, 16);
        if (hex.length() == 6)
            return new Color((0xFF << 24) | val);
        return new Color(val);
    }

    public static Color fromHSB(float hue, float saturation, float brightness) {
        int color = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
        return new Color(color);
    }

    public float getRed() {
        return r;
    }

    public float getGreen() {
        return g;
    }

    public float getBlue() {
        return b;
    }

    public float getAlpha() {
        return a;
    }

    public int getColorAsInt() {
        int ai = (int) (a * 255.0f + 0.5f);
        int ri = (int) (r * 255.0f + 0.5f);
        int gi = (int) (g * 255.0f + 0.5f);
        int bi = (int) (b * 255.0f + 0.5f);
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }

    public static Color interpolate(Color start, Color end, float delta) {
        float r = MathHelper.lerp(delta, start.r, end.r);
        float g = MathHelper.lerp(delta, start.g, end.g);
        float b = MathHelper.lerp(delta, start.b, end.b);
        float a = MathHelper.lerp(delta, start.a, end.a);
        return new Color(r, g, b, a);
    }
}
