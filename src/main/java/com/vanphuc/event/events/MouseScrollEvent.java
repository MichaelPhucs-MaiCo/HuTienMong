package com.vanphuc.event.events;

public class MouseScrollEvent extends AbstractEvent {
    private final double horizontal;
    private final double vertical;

    public MouseScrollEvent(double horizontal, double vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public double getVertical() { return vertical; }
    public double getHorizontal() { return horizontal; }
}