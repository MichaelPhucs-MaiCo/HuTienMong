package com.vanphuc.event.listeners;

import com.vanphuc.event.events.MouseClickEvent;

public interface MouseClickListener extends AbstractListener {
    public abstract void onMouseClick(MouseClickEvent mouseClickEvent);
}
