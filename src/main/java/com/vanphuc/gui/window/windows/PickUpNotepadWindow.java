package com.vanphuc.gui.window.windows;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.window.BaseNotepadWindow;
import com.vanphuc.module.modules.AutoPickUp;
import java.util.List;

public class PickUpNotepadWindow extends BaseNotepadWindow {
    private final AutoPickUp module;

    public PickUpNotepadWindow(AutoPickUp module, Rectangle position) {
        super("§l" + module.name + " Priority List", position);
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