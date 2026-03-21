package com.vanphuc.event.events;

public class MouseClickEvent extends AbstractEvent {
    public final double mouseX;
    public final double mouseY;
    public final int button;
    public final int action;
    public final int buttonNumber;
    public final int mods;

    public MouseClickEvent(double mouseX, double mouseY, int button, int action, int mods) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.button = button;
        this.action = action;
        this.buttonNumber = -1;
        this.mods = mods;
    }

    public MouseClickEvent(double mouseX, double mouseY, int button, int action, int mods, int buttonNumber) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.button = button;
        this.action = action;
        this.mods = mods;
        this.buttonNumber = buttonNumber;
    }
}