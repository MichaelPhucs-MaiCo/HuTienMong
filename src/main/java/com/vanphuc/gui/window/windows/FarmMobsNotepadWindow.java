package com.vanphuc.gui.window.windows;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.window.BaseNotepadWindow;
import com.vanphuc.module.modules.FarmCustomMobs;
import java.util.List;

public class FarmMobsNotepadWindow extends BaseNotepadWindow {
    private final FarmCustomMobs module;

    public FarmMobsNotepadWindow(FarmCustomMobs module, Rectangle position) {
        super("§l" + module.name + " Target List", position);
        this.module = module;
        refresh();
    }

    public void refresh() {
        loadData(module.getListContent());
    }

    @Override
    protected void onSave(List<String> data) {
        module.updateList(data);
    }
}