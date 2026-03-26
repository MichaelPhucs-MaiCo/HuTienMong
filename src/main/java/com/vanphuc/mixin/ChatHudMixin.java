package com.vanphuc.mixin;

import com.vanphuc.HuTienMongClient;
import com.vanphuc.event.game.ReceiveMessageEvent;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    // Mixin vào phương thức addMessage chính của 1.21.4
    @Inject(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci) {
        // Post event lên hệ thống của Hư Tiên Mộng
        // Vì trong method này không có messageId trực tiếp, ta truyền vào 0 (tương đương nextId của Meteor khi không xác định)
        ReceiveMessageEvent event = HuTienMongClient.EVENT_BUS.post(ReceiveMessageEvent.get(message, indicator, 0));

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}