
package com.vanphuc.event.listeners;

import com.vanphuc.event.events.MouseScrollEvent;

public interface MouseScrollListener extends AbstractListener {
    public abstract void onMouseScroll(MouseScrollEvent event);
}
