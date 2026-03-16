package com.vanphuc.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vanphuc.gui.colors.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class RenderWorldUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void drawCircle(MatrixStack matrices, Vec3d anchorPos, double radius, Color color) {
        if (mc.world == null || mc.gameRenderer == null || anchorPos == null) return;

        // Lấy tọa độ Camera
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        // Tính khoảng cách từ Camera đến Tâm (Chuẩn Mod-Public)
        double x = anchorPos.x - cameraPos.x;
        double y = anchorPos.y - cameraPos.y;
        double z = anchorPos.z - cameraPos.z;

        setup3DRender();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        int argb = color.getColorAsInt();
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        float a = ((argb >> 24) & 0xFF) / 255f;

        int segments = 60;
        for (int i = 0; i <= segments; ++i) {
            double angle = 6.283185307179586 * (double)i / (double)segments;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;

            // Cộng trực tiếp offset vào vertex giống hệt mã gốc
            buffer.vertex(matrix, (float)(x + dx), (float)(y + 0.1), (float)(z + dz)).color(r, g, b, a);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        end3DRender();
    }

    private static void setup3DRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(2.0F);
    }

    private static void end3DRender() {
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}