package com.vanphuc;

import com.vanphuc.gui.GuiManager;
import com.vanphuc.module.Module;
import com.vanphuc.module.Modules;
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

        // 3. Vòng lặp Logic (Cực kỳ quan trọng cho các tính năng tự động)
        // Fabric sẽ gọi hàm này 20 lần mỗi giây (20 Ticks) [cite: 1135]
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null)
                return;

            // CHỈNH SỬA TẠI ĐÂY: Chạy logic update cho TẤT CẢ các module đang bật
            // Cậu không cần check 'instanceof' nữa, giúp code cực kỳ sạch sẽ!
            for (Module module : Modules.get().getAll()) {
                if (module.isActive()) {
                    module.onUpdate();
                }
            }
        });
    }
}