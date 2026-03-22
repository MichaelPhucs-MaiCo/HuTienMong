package com.vanphuc.gui.window.windows;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.window.BaseNotepadWindow;
import com.vanphuc.module.modules.AutoSwitchHotbar;
import java.util.List;

public class ListSwitchHotbar extends BaseNotepadWindow {
    private final AutoSwitchHotbar module;

    public ListSwitchHotbar(AutoSwitchHotbar module, Rectangle position) {
        // 1. Kế thừa tiêu đề từ Base
        super("§l" + module.name + " List", position);
        this.module = module;

        // 2. Nạp dữ liệu vào
        loadData(module.getListContent());
    }

    @Override
    protected void onSave(List<String> data) {
        // 3. Khi người dùng sửa xong, ném data về cho Module xử lý
        module.updateList(data);
    }
}