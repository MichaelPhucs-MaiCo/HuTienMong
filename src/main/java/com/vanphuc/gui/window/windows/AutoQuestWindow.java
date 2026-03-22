package com.vanphuc.gui.window.windows;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.window.BaseNotepadWindow;
import com.vanphuc.module.modules.AutoQuest;
import com.vanphuc.module.settings.StringListSetting;
import java.util.List;

public class AutoQuestWindow extends BaseNotepadWindow {
    private final AutoQuest module;
    private final StringListSetting targetList;

    public AutoQuestWindow(AutoQuest module, StringListSetting targetList, Rectangle position) {
        super("§l" + module.name + " - " + targetList.getName(), position);
        this.module = module;
        this.targetList = targetList;
        refresh();
    }

    public void refresh() {
        loadData(targetList.getValue());
    }

    @Override
    protected void onSave(List<String> data) {
        targetList.setValue(data);
    }
}