package com.vanphuc;

import com.vanphuc.gui.GuiManager;
import com.vanphuc.module.Module;
import com.vanphuc.module.Modules;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.lang.invoke.MethodHandles;

public class HuTienMongClient implements ClientModInitializer {
    // Khởi tạo Event Bus của Orbit làm "trung tâm điều phối" sự kiện
    public static final IEventBus EVENT_BUS = new EventBus();

    @Override
    public void onInitializeClient() {
        // 1. Vòng lặp Render (Để vẽ ClickGUI và HUD)
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            GuiManager.getInstance().render(drawContext, renderTickCounter.getTickDelta(true));
        });

        // 2. Vòng lặp Logic (20 Ticks mỗi giây)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Chạy logic update cho tất cả các module đang ở trạng thái bật (Active)
            for (Module module : Modules.get().getAll()) {
                if (module.isActive()) {
                    module.onUpdate();
                }
            }
        });

        // Log thông báo Client đã sẵn sàng
        System.out.println("HuTienMong Client đã khởi tải thành công với Orbit Event Bus!");
    }
}