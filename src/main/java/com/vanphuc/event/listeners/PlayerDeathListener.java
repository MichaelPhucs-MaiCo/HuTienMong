
package com.vanphuc.event.listeners;

import com.vanphuc.event.events.PlayerDeathEvent;

public interface PlayerDeathListener extends AbstractListener {
    public abstract void onPlayerDeath(PlayerDeathEvent readPacketEvent);
}
