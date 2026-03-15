package com.vanphuc.event.listeners;

import com.vanphuc.event.events.GameLeftEvent;

public interface GameLeftListener extends AbstractListener {
    public abstract void onGameLeft(GameLeftEvent event);
}
