package com.vanphuc.module;

import com.vanphuc.module.modules.*;
import com.vanphuc.module.modules_test.TestDropdown;

import java.util.ArrayList;
import java.util.List;

public class Modules {
    private static Modules INSTANCE;
    private final List<Module> modules = new ArrayList<>();

    public Modules() {
        // Sau này tạo module nào thì đăng ký ở đây
        // add(new Flight());

        /*add(new TestRender());
        add(new TestInventory());
        add(new TestBaritone());
        add(new HudTest());
        add(new TestDropdown());*/


        add(new AutoClicker());
        add(new AutoSwitchHotbar());
        add(new AutoSavePaper());
        add(new AutoPickUp());
        add(new FarmCustomMobs());
        add(new CopyDataComp());
        add(new AutoQuest());
        add(new AntiBlind());
        add(new NoWeather());
        add(new Flight());
    }

    public static Modules get() {
        if (INSTANCE == null) INSTANCE = new Modules();
        return INSTANCE;
    }

    public void add(Module module) {
        modules.add(module);
    }

    public List<Module> getAll() {
        return modules;
    }

    public <T extends Module> T get(Class<T> klass) {
        for (Module m : modules) {
            if (m.getClass() == klass) return (T) m;
        }
        return null;
    }
}