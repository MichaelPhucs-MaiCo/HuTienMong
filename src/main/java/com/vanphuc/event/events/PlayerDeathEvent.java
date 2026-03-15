

package com.vanphuc.event.events;

import com.vanphuc.event.listeners.AbstractListener;
import com.vanphuc.event.listeners.PlayerDeathListener;

import java.util.ArrayList;
import java.util.List;

public class PlayerDeathEvent extends AbstractEvent {
    public PlayerDeathEvent() {
    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            PlayerDeathListener playerDeathListener = (PlayerDeathListener) listener;
            playerDeathListener.onPlayerDeath(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<PlayerDeathListener> GetListenerClassType() {
        return PlayerDeathListener.class;
    }
}