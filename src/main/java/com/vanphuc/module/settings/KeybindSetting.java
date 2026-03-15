package com.vanphuc.module.settings;

import com.vanphuc.module.Module;
import org.lwjgl.glfw.GLFW;

public class KeybindSetting extends Setting<Integer> {
    private int modifiers = 0;
    private final Module module;

    public KeybindSetting(String name, int defaultKey, Module module) {
        super(name, defaultKey);
        this.module = module;
    }

    public int getKey() {
        return getValue();
    }

    public int getModifiers() {
        return modifiers;
    }

    public Module getModule() {
        return module;
    }

    public void setKey(int key, int modifiers) {
        setValue(key);
        this.modifiers = modifiers;
    }

    public boolean isBound() {
        return getValue() != GLFW.GLFW_KEY_UNKNOWN && getValue() != -1;
    }

    // Kiểm tra xem phím bấm và phím bổ trợ có khớp không
    public boolean matches(int key, int mods) {
        if (!isBound()) return false;
        return this.getKey() == key && this.modifiers == mods;
    }

    // Tạo chuỗi hiển thị đẹp mắt (VD: LCTRL + R)
    public String getFormattedKey() {
        if (!isBound()) return "None";
        StringBuilder builder = new StringBuilder();

        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) builder.append("CTRL + ");
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) builder.append("SHIFT + ");
        if ((modifiers & GLFW.GLFW_MOD_ALT) != 0) builder.append("ALT + ");

        String keyName = GLFW.glfwGetKeyName(getKey(), 0);
        if (keyName != null) {
            builder.append(keyName.toUpperCase());
        } else {
            // Tên fallback cho các phím đặc biệt không có tên char
            switch (getKey()) {
                case GLFW.GLFW_KEY_ENTER: builder.append("ENTER"); break;
                case GLFW.GLFW_KEY_TAB: builder.append("TAB"); break;
                case GLFW.GLFW_KEY_SPACE: builder.append("SPACE"); break;
                case GLFW.GLFW_KEY_UP: builder.append("UP"); break;
                case GLFW.GLFW_KEY_DOWN: builder.append("DOWN"); break;
                case GLFW.GLFW_KEY_LEFT: builder.append("LEFT"); break;
                case GLFW.GLFW_KEY_RIGHT: builder.append("RIGHT"); break;
                default: builder.append("KEY ").append(getKey()); break;
            }
        }
        return builder.toString();
    }
}