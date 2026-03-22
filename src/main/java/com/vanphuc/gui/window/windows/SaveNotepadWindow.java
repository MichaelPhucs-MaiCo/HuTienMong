package com.vanphuc.gui.window.windows;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.window.BaseNotepadWindow;
import com.vanphuc.module.modules.AutoSavePaper;
import java.util.List;

public class SaveNotepadWindow extends BaseNotepadWindow {
    private final AutoSavePaper module;

    public SaveNotepadWindow(AutoSavePaper module, Rectangle position) {
        super("§l" + module.name + " Save List", position);
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