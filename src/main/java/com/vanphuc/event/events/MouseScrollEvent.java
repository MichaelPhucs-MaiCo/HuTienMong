

package com.vanphuc.event.events;

import com.vanphuc.event.listeners.AbstractListener;
import com.vanphuc.event.listeners.MouseScrollListener;

import java.util.ArrayList;
import java.util.List;

public class MouseScrollEvent extends AbstractEvent {
    private double horizontal;
    private double vertical;

    public MouseScrollEvent(double horizontal2, double vertical2) {
        super();
        this.horizontal = horizontal2;
        this.vertical = vertical2;
    }

    public double GetVertical() {
        return vertical;
    }

    public double GetHorizontal() {
        return horizontal;
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            MouseScrollListener mouseScrollListener = (MouseScrollListener) listener;
            mouseScrollListener.onMouseScroll(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<MouseScrollListener> GetListenerClassType() {
        return MouseScrollListener.class;
    }
}