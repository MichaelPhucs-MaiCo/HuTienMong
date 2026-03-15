package com.vanphuc;

import com.vanphuc.gui.GuiManager;
import com.vanphuc.module.Module;
import com.vanphuc.module.Modules;
import com.vanphuc.module.modules.AutoClicker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class HuTienMongClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 1. Vòng lặp Render (Để vẽ ClickGUI và HUD)
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            GuiManager.getInstance().render(drawContext, renderTickCounter.getTickDelta(true));
        });

        // 3. Vòng lặp Logic (Quan trọng để AutoClicker hoạt động)
        // Fabric sẽ gọi hàm này 20 lần mỗi giây (20 Ticks)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null)
                return;

            // Chạy logic update cho tất cả các module đang bật
            for (Module module : Modules.get().getAll()) {
                if (module.isActive()) {
                    // Nếu là AutoClicker thì gọi hàm update riêng của nó
                    if (module instanceof AutoClicker autoClicker) {
                        autoClicker.onUpdate();
                    }

                    // Sau này ông thêm hàm onTick() vào class Module gốc
                    // thì chỉ cần gọi module.onTick() ở đây là xong!
                }
            }
        });
    }
}