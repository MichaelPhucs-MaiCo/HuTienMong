package com.vanphuc.gui;

import com.vanphuc.gui.colors.Color;
import com.vanphuc.module.modules.AutoSavePaper;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class TrashNotepadWindow extends Window {
    private final AutoSavePaper module;
    private final List<StringBuilder> lines = new ArrayList<>();

    private int cursorLine = 0;
    private int cursorCol = 0;
    private int selectLine = -1;
    private int selectCol = -1;
    private boolean isDraggingText = false;

    public TrashNotepadWindow(AutoSavePaper module, Rectangle position) {
        super("§l" + module.name + " List", position);
        this.module = module;

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

    // Rest of implementation is same as NotepadWindow but calling module.updateList on save.
    // ---- for brevity, copy the implementation from NotepadWindow with the following saveData() ----
    private void saveData() {
        List<String> saved = new ArrayList<>();
        for (StringBuilder sb : lines) saved.add(sb.toString());
        module.updateList(saved);
        com.vanphuc.utils.ConfigManager.save();
    }

    // Include all other methods (draw, onMouseClick, onKey, insertText, deleteSelection etc.)
    // You can reuse the NotepadWindow implementation but ensure saveData calls module.updateList(...)
    // (Full code identical to NotepadWindow but with module type AutoSavePaper)
}