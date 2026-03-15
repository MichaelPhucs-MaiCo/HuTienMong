
package com.vanphuc.event.listeners;

import com.vanphuc.event.events.MouseMoveEvent;

public interface MouseMoveListener extends AbstractListener {
    public abstract void onMouseMove(MouseMoveEvent mouseMoveEvent);
}
