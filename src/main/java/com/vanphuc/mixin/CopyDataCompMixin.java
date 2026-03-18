package com.vanphuc.mixin;

import com.vanphuc.module.Modules;
import com.vanphuc.module.modules.CopyDataComp;
import com.vanphuc.utils.ChatUtils;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class CopyDataCompMixin extends net.minecraft.client.gui.screen.Screen {

    protected CopyDataCompMixin(net.minecraft.text.Text title) {
        super(title);
    }

    @Shadow protected Slot focusedSlot;
    // Lôi cái hàm click nội bộ của game ra xài
    @Shadow protected abstract void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType);

    // ==========================================
    // 1. CHẶN CLICK TRÁI ĐỂ COPY
    // ==========================================
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClickedCopy(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        CopyDataComp module = Modules.get().get(CopyDataComp.class);
        if (module != null && module.isActive() && button == 0) {
            if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
                ItemStack stack = this.focusedSlot.getStack();
                String data = this.focusedSlot.id + ":" + stack.getComponents().toString();

                if (this.client != null && this.client.keyboard != null) {
                    this.client.keyboard.setClipboard(data);
                    ChatUtils.info("CopyData", "📋 §a§lĐÃ COPY SLOT " + this.focusedSlot.id + "!");
                }
                cir.setReturnValue(true);
            }
        }
    }

    // ==========================================
    // 2. BIẾN CUỘN CHUỘT THÀNH CLICK TRÁI TẠM THỜI
    // ==========================================
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolledFakeClick(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        CopyDataComp module = Modules.get().get(CopyDataComp.class);
        // Nếu module bật và công tắc cuộn chuột đang ON
        if (module != null && module.isActive() && module.useScrollToClick.isEnabled()) {
            if (this.focusedSlot != null) {
                // Mô phỏng 1 cú click trái (button 0, action PICKUP) thẳng vào slot đang trỏ
                this.onMouseClick(this.focusedSlot, this.focusedSlot.id, 0, SlotActionType.PICKUP);
                cir.setReturnValue(true);
            }
        }
    }

    // ==========================================
    // 3. BIẾN PHÍM PHỤ THÀNH CLICK TRÁI (NẾU CÓ CÀI)
    // ==========================================
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressedFakeClick(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        CopyDataComp module = Modules.get().get(CopyDataComp.class);
        if (module != null && module.isActive() && module.altClickKey.isBound()) {
            // Kiểm tra xem phím vừa bấm có trùng với phím đã gán trong setting không
            if (module.altClickKey.matches(keyCode, modifiers)) {
                if (this.focusedSlot != null) {
                    this.onMouseClick(this.focusedSlot, this.focusedSlot.id, 0, SlotActionType.PICKUP);
                    cir.setReturnValue(true);
                }
            }
        }
    }
}