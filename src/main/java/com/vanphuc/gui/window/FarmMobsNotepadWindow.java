package com.vanphuc.gui.window;

import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.Window;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.module.modules.FarmCustomMobs;
import com.vanphuc.utils.ConfigManager;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class FarmMobsNotepadWindow extends Window {
    private final FarmCustomMobs module;
    private final List<StringBuilder> lines = new ArrayList<>();
    private int cursorLine = 0;
    private int cursorCol = 0;
    private int selectLine = -1;
    private int selectCol = -1;
    private boolean isDraggingText = false;

    public FarmMobsNotepadWindow(FarmCustomMobs module, Rectangle position) {
        super("§l" + module.name + " Target List", position);
        this.module = module;
        refresh();
    }

    public void refresh() {
        lines.clear();
        if (module.getListContent().isEmpty()) {
            lines.add(new StringBuilder());
        } else {
            for (String s : module.getListContent()) {
                lines.add(new StringBuilder(s));
            }
        }
        cursorLine = lines.size() - 1;
        cursorCol = lines.get(cursorLine).length();
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        this.position.setHeight(Math.max(100f, titleHeight + 16f + (lines.size() * 12f)));
        super.draw(context, partialTicks);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        float startX = position.getX() + 6;
        float startY = position.getY() + titleHeight + 6;
        MinecraftClient mc = MinecraftClient.getInstance();

        int[] sel = hasSelection() ? getNormalizedSelection() : null;

        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i).toString();
            float lineY = startY + (i * 12);

            // Bôi đen màu xanh Blue (#3B82F6) cho chuẩn Sleek Carbon
            if (sel != null && i >= sel[0] && i <= sel[2]) {
                int sCol = (i == sel[0]) ? sel[1] : 0;
                int eCol = (i == sel[2]) ? sel[3] : text.length();
                float x1 = startX + mc.textRenderer.getWidth(text.substring(0, sCol));
                float x2 = startX + mc.textRenderer.getWidth(text.substring(0, eCol));
                if (i < sel[2] && eCol == text.length()) x2 += 4;
                Render2D.drawBox(matrix, x1, lineY - 1, x2 - x1, 11, new Color(0x663B82F6));
            }

            Render2D.drawString(context, mc.textRenderer, text, startX, lineY, new Color(0xFFFFFFFF));

            if (i == cursorLine && !hasSelection() && (System.currentTimeMillis() / 500) % 2 == 0) {
                float cx = startX + mc.textRenderer.getWidth(text.substring(0, cursorCol));
                Render2D.drawBox(matrix, cx, lineY - 1, 1, 10, new Color(0xFFFFFFFF));
            }
        }
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        boolean inTitle = mouseX >= position.getX() && mouseX <= position.getX() + position.getWidth() &&
                mouseY >= position.getY() && mouseY <= position.getY() + titleHeight;

        if (button == 0 && pressed && inTitle) {
            isMoving = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        } else if (button == 0 && !pressed) {
            isMoving = false;
            isDraggingText = false;
        }

        float startX = position.getX() + 6;
        float startY = position.getY() + titleHeight + 6;

        if (button == 0 && pressed && !inTitle && mouseX >= position.getX() && mouseX <= position.getX() + position.getWidth() &&
                mouseY >= startY && mouseY <= position.getY() + position.getHeight()) {
            updateCursorByMouse(mouseX, mouseY, true);
            isDraggingText = true;
            return true;
        }

        return super.onMouseClick(mouseX, mouseY, button, pressed) || inTitle;
    }

    @Override
    public void onMouseMove(double mouseX, double mouseY) {
        if (isMoving) {
            position.setX(position.getX() + (float) (mouseX - lastMouseX));
            position.setY(position.getY() + (float) (mouseY - lastMouseY));
            this.arrange(this.position);
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        } else if (isDraggingText) {
            updateCursorByMouse(mouseX, mouseY, false);
        }
        super.onMouseMove(mouseX, mouseY);
    }

    private void updateCursorByMouse(double mouseX, double mouseY, boolean isClick) {
        float startX = position.getX() + 6;
        float startY = position.getY() + titleHeight + 6;
        int lineIdx = (int) ((mouseY - startY) / 12);
        lineIdx = Math.max(0, Math.min(lines.size() - 1, lineIdx));
        String text = lines.get(lineIdx).toString();
        int colIdx = 0;
        MinecraftClient mc = MinecraftClient.getInstance();

        for (int i = 0; i <= text.length(); i++) {
            float w = mc.textRenderer.getWidth(text.substring(0, i));
            if (startX + w >= mouseX) {
                if (i > 0) {
                    float wPrev = mc.textRenderer.getWidth(text.substring(0, i - 1));
                    if (mouseX - (startX + wPrev) < (startX + w) - mouseX) { colIdx = i - 1; break; }
                }
                colIdx = i;
                break;
            }
            colIdx = text.length();
        }

        boolean shift = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

        if (isClick && !shift) {
            selectLine = -1;
            selectCol = -1;
        } else {
            if (!hasSelection() && (shift || isDraggingText)) { selectLine = cursorLine; selectCol = cursorCol; }
        }
        cursorLine = lineIdx;
        cursorCol = colIdx;
    }

    @Override
    public boolean onChar(int codePoint, int modifiers) {
        if (codePoint >= 32 && codePoint != 127) {
            insertText(new String(Character.toChars(codePoint)));
            return true;
        }
        return super.onChar(codePoint, modifiers);
    }

    @Override
    public boolean onKey(int key, int action, int mods) {
        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                GuiManager.getInstance().removeWindow(this);
                ConfigManager.save();
                GuiManager.getInstance().toggle();
                return true;
            }
            MinecraftClient mc = MinecraftClient.getInstance();
            boolean shift = (mods & GLFW.GLFW_MOD_SHIFT) != 0;
            boolean ctrl = (mods & GLFW.GLFW_MOD_CONTROL) != 0;

            if (ctrl) {
                if (key == GLFW.GLFW_KEY_A) { selectLine = 0; selectCol = 0; cursorLine = lines.size() - 1; cursorCol = lines.get(cursorLine).length(); return true; }
                else if (key == GLFW.GLFW_KEY_C) { mc.keyboard.setClipboard(getSelectedText()); return true; }
                else if (key == GLFW.GLFW_KEY_X) { mc.keyboard.setClipboard(getSelectedText()); deleteSelection(); return true; }
                else if (key == GLFW.GLFW_KEY_V) { insertText(mc.keyboard.getClipboard()); return true; }
            }

            if (key == GLFW.GLFW_KEY_LEFT) { moveCursor(-1, shift); return true; }
            else if (key == GLFW.GLFW_KEY_RIGHT) { moveCursor(1, shift); return true; }
            else if (key == GLFW.GLFW_KEY_UP) { moveCursorVertical(-1, shift); return true; }
            else if (key == GLFW.GLFW_KEY_DOWN) { moveCursorVertical(1, shift); return true; }

            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (hasSelection()) { deleteSelection(); }
                else {
                    if (cursorCol > 0) { lines.get(cursorLine).deleteCharAt(cursorCol - 1); cursorCol--; saveData(); }
                    else if (cursorLine > 0) {
                        String rem = lines.get(cursorLine).toString();
                        lines.remove(cursorLine); cursorLine--;
                        cursorCol = lines.get(cursorLine).length();
                        lines.get(cursorLine).append(rem);
                        saveData();
                    }
                }
                return true;
            } else if (key == GLFW.GLFW_KEY_DELETE) {
                if (hasSelection()) { deleteSelection(); }
                else {
                    if (cursorCol < lines.get(cursorLine).length()) { lines.get(cursorLine).deleteCharAt(cursorCol); saveData(); }
                    else if (cursorLine < lines.size() - 1) {
                        String rem = lines.get(cursorLine + 1).toString();
                        lines.remove(cursorLine + 1); lines.get(cursorLine).append(rem); saveData();
                    }
                }
                return true;
            } else if (key == GLFW.GLFW_KEY_ENTER) {
                if (hasSelection()) deleteSelection();
                String rem = lines.get(cursorLine).substring(cursorCol);
                lines.get(cursorLine).setLength(cursorCol);
                cursorLine++; cursorCol = 0;
                lines.add(cursorLine, new StringBuilder(rem));
                saveData();
                return true;
            }
        }
        return super.onKey(key, action, mods);
    }

    private void moveCursor(int offset, boolean shift) {
        if (shift && !hasSelection()) { selectLine = cursorLine; selectCol = cursorCol; }
        else if (!shift && hasSelection()) { selectLine = -1; selectCol = -1; }

        if (offset == -1) {
            if (cursorCol > 0) cursorCol--;
            else if (cursorLine > 0) { cursorLine--; cursorCol = lines.get(cursorLine).length(); }
        } else if (offset == 1) {
            if (cursorCol < lines.get(cursorLine).length()) cursorCol++;
            else if (cursorLine < lines.size() - 1) { cursorLine++; cursorCol = 0; }
        }
    }

    private void moveCursorVertical(int offset, boolean shift) {
        if (shift && !hasSelection()) { selectLine = cursorLine; selectCol = cursorCol; }
        else if (!shift && hasSelection()) { selectLine = -1; selectCol = -1; }

        if (offset == -1 && cursorLine > 0) { cursorLine--; cursorCol = Math.min(cursorCol, lines.get(cursorLine).length()); }
        else if (offset == 1 && cursorLine < lines.size() - 1) { cursorLine++; cursorCol = Math.min(cursorCol, lines.get(cursorLine).length()); }
    }

    private boolean hasSelection() { return selectLine != -1 && (selectLine != cursorLine || selectCol != cursorCol); }

    private int[] getNormalizedSelection() {
        if (selectLine < cursorLine || (selectLine == cursorLine && selectCol < cursorCol)) return new int[]{selectLine, selectCol, cursorLine, cursorCol};
        else return new int[]{cursorLine, cursorCol, selectLine, selectCol};
    }

    private String getSelectedText() {
        if (!hasSelection()) return "";
        int[] sel = getNormalizedSelection();
        int sl = sel[0], sc = sel[1], el = sel[2], ec = sel[3];

        if (sl == el) return lines.get(sl).substring(sc, ec);
        StringBuilder sb = new StringBuilder();
        sb.append(lines.get(sl).substring(sc)).append("\n");
        for (int i = sl + 1; i < el; i++) sb.append(lines.get(i)).append("\n");
        sb.append(lines.get(el).substring(0, ec));
        return sb.toString();
    }

    private void deleteSelection() {
        if (!hasSelection()) return;
        int[] sel = getNormalizedSelection();
        int sl = sel[0], sc = sel[1], el = sel[2], ec = sel[3];

        StringBuilder startStr = new StringBuilder(lines.get(sl).substring(0, sc));
        String endStr = lines.get(el).substring(ec);
        for (int i = el; i > sl; i--) lines.remove(i);
        lines.set(sl, startStr.append(endStr));

        cursorLine = sl; cursorCol = sc; selectLine = -1; selectCol = -1;
        saveData();
    }

    private void insertText(String str) {
        if (hasSelection()) deleteSelection();
        String[] parts = str.replace("\r", "").split("\n", -1);
        if (parts.length == 1) {
            lines.get(cursorLine).insert(cursorCol, parts[0]);
            cursorCol += parts[0].length();
        } else {
            String remainder = lines.get(cursorLine).substring(cursorCol);
            lines.get(cursorLine).setLength(cursorCol);
            lines.get(cursorLine).append(parts[0]);
            for (int i = 1; i < parts.length - 1; i++) { cursorLine++; lines.add(cursorLine, new StringBuilder(parts[i])); }
            cursorLine++; lines.add(cursorLine, new StringBuilder(parts[parts.length - 1]).append(remainder));
            cursorCol = parts[parts.length - 1].length();
        }
        saveData();
    }

    private void saveData() {
        List<String> saved = new ArrayList<>();
        for (StringBuilder sb : lines) {
            if (!sb.toString().trim().isEmpty()) saved.add(sb.toString().trim());
        }
        module.updateList(saved);
        ConfigManager.save();
    }
}