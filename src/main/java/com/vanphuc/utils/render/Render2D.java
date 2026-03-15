package com.vanphuc.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vanphuc.gui.colors.Color;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class Render2D {

    /**
     * PHƯƠNG THỨC SIÊU MỊN: Vẽ 3 lớp layer (AA + Blur) để đạt độ mượt như Aoba-Client.
     */
    public static void drawSmoothRoundedBox(Matrix4f matrix, float x, float y, float width, float height, float radius, Color color) {
        int packed = color.getColorAsInt();
        int r = (packed >> 16) & 0xFF;
        int g = (packed >> 8)  & 0xFF;
        int b = (packed)       & 0xFF;
        int a = (packed >> 24) & 0xFF;

        // Vẽ 2 lớp shadow/blur siêu mỏng ở rìa để làm mịn cạnh (Anti-Aliasing)
        drawRoundedBoxInternal(matrix, x - 0.8f, y - 0.8f, width + 1.6f, height + 1.6f, radius + 0.8f, pack(r, g, b, (int)(a * 0.3f)));
        drawRoundedBoxInternal(matrix, x - 0.4f, y - 0.4f, width + 0.8f, height + 0.8f, radius + 0.4f, pack(r, g, b, (int)(a * 0.6f)));

        // Vẽ lớp chính
        drawRoundedBoxInternal(matrix, x, y, width, height, radius, packed);
    }

    /**
     * Logic 9-Part No-Overlap: Khớp khít 100%, triệt tiêu nốt ruồi và lỗi xuyên thấu.
     */
    private static void drawRoundedBoxInternal(Matrix4f matrix, float x, float y, float w, float h, float radius, int color) {
        float r = Math.min(radius, Math.min(w / 2f, h / 2f));
        setupRender();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        // 1. Lõi trung tâm (KHÔNG CHẠM RÌA)
        addQuad(buffer, matrix, x + r, y + r, w - r * 2, h - r * 2, color);
        // 2. 4 Cạnh (Trên, Dưới, Trái, Phải) - KHÔNG đè lên nhau
        addQuad(buffer, matrix, x + r, y, w - r * 2, r, color); // Top
        addQuad(buffer, matrix, x + r, y + h - r, w - r * 2, r, color); // Bottom
        addQuad(buffer, matrix, x, y + r, r, h - r * 2, color); // Left
        addQuad(buffer, matrix, x + w - r, y + r, r, h - r * 2, color); // Right

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // 3. Vẽ 4 góc bo dùng TRIANGLE_FAN để xóa bỏ "nốt ruồi" ở tâm
        int segs = (int) Math.max(r * 3.0f, 24);
        drawCornerFan(matrix, x + r, y + r, r, 180, segs, color); // TL
        drawCornerFan(matrix, x + w - r, y + r, r, 270, segs, color); // TR
        drawCornerFan(matrix, x + w - r, y + h - r, r, 0, segs, color); // BR
        drawCornerFan(matrix, x + r, y + h - r, r, 90, segs, color); // BL

        endRender();
    }

    private static void drawCornerFan(Matrix4f matrix, float cx, float cy, float r, float start, int segs, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, cx, cy, 0).color(color);
        for (int i = 0; i <= segs; i++) {
            float rad = (float) Math.toRadians(start + i * (90f / segs));
            buffer.vertex(matrix, cx + (float) Math.cos(rad) * r, cy + (float) Math.sin(rad) * r, 0).color(color);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    // Các hàm phụ trợ tối ưu
    private static void addQuad(BufferBuilder b, Matrix4f m, float x, float y, float w, float h, int c) {
        if (w <= 0 || h <= 0) return;
        b.vertex(m, x, y, 0).color(c); b.vertex(m, x, y+h, 0).color(c); b.vertex(m, x+w, y+h, 0).color(c);
        b.vertex(m, x, y, 0).color(c); b.vertex(m, x+w, y+h, 0).color(c); b.vertex(m, x+w, y, 0).color(c);
    }

    private static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.disableCull();
        // ÉP BUỘC vẽ trên cùng
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
    }

    private static void endRender() {
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static int pack(int r, int g, int b, int a) {
        return (Math.max(0, Math.min(255, a)) << 24) | (r << 16) | (g << 8) | b;
    }

    // --- Các hàm tiện ích khác (Giữ nguyên) ---
    public static void drawBox(Matrix4f matrix, float x, float y, float width, float height, Color color) {
        setupRender();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        addQuad(buffer, matrix, x, y, width, height, color.getColorAsInt());
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    public static void drawRoundedOutline(Matrix4f matrix, float x, float y, float width, float height, float radius, float thickness, Color color) {
        setupRender();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        int c = color.getColorAsInt();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        int segs = 24;
        for (int i = 0; i <= segs; i++) addVA(buffer, matrix, x+width-radius, y+radius, radius, 270+i*(90f/segs), c);
        for (int i = 0; i <= segs; i++) addVA(buffer, matrix, x+width-radius, y+height-radius, radius, 0+i*(90f/segs), c);
        for (int i = 0; i <= segs; i++) addVA(buffer, matrix, x+radius, y+height-radius, radius, 90+i*(90f/segs), c);
        for (int i = 0; i <= segs; i++) addVA(buffer, matrix, x+radius, y+radius, radius, 180+i*(90f/segs), c);
        addVA(buffer, matrix, x+width-radius, y+radius, radius, 270, c);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        endRender();
    }

    private static void addVA(BufferBuilder b, Matrix4f m, float cx, float cy, float r, float a, int c) {
        float rad = (float) Math.toRadians(a);
        b.vertex(m, cx + (float) Math.cos(rad)*r, cy + (float) Math.sin(rad)*r, 0).color(c);
    }

    public static void drawItemIcon(DrawContext context, ItemStack stack, int x, int y) { context.drawItem(stack, x, y); }
    public static void drawString(DrawContext context, TextRenderer textRenderer, String text, float x, float y, Color color) {
        context.drawText(textRenderer, text, (int) x, (int) y, color.getColorAsInt(), false);
    }
    public static void drawOverlay(DrawContext context, int width, int height) {
        setupRender(); context.fill(0, 0, width, height, 0x90000000); endRender();
    }
}