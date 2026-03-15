package com.vanphuc.event.events;

import com.vanphuc.event.listeners.AbstractListener;
import com.vanphuc.event.listeners.GameLeftListener;

import java.util.ArrayList;
import java.util.List;

public class GameLeftEvent extends AbstractEvent {

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            GameLeftListener gameLeftListener = (GameLeftListener) listener;
            gameLeftListener.onGameLeft(this);
        }
    }

    @Override
    public Class<GameLeftListener> GetListenerClassType() {
        return GameLeftListener.class;
    }
}
