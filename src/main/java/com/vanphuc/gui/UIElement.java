package com.vanphuc.gui;

import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

public abstract class UIElement {
    public boolean visible = true;
    protected UIElement parent;
    public final List<UIElement> children = new ArrayList<>();
    protected Rectangle position = new Rectangle(0, 0, 0, 0);
    protected Margin margin = new Margin(0, 0, 0, 0);
    protected boolean isHovered = false;

    public void initialize() {
        for (UIElement child : children) {
            child.initialize();
        }
    }

    public void update() {
        for (UIElement child : children) {
            child.update();
        }
    }

    public void draw(DrawContext context, float partialTicks) {
        if (!visible) return;
        for (UIElement child : children) {
            child.draw(context, partialTicks);
        }
    }

    public void measure(Size availableSize) {
        // Default implementation logic can be overridden
        for (UIElement child : children) {
            child.measure(availableSize);
        }
    }

    public void arrange(Rectangle finalSize) {
        this.position = finalSize;
        // Default logic: children follow parent
        for (UIElement child : children) {
            child.arrange(
                    new Rectangle(finalSize.getX(), finalSize.getY(), finalSize.getWidth(), finalSize.getHeight()));
        }
    }

    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        if (!visible) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).onMouseClick(mouseX, mouseY, button, pressed)) {
                return true;
            }
        }
        return false;
    }

    public void onMouseMove(double mouseX, double mouseY) {
        this.isHovered = position.contains(mouseX, mouseY);
        for (UIElement child : children) {
            child.onMouseMove(mouseX, mouseY);
        }
    }

    public void addChild(UIElement child) {
        child.parent = this;
        children.add(child);
    }

    public void removeChild(UIElement child) {
        child.parent = null;
        children.remove(child);
    }

    public boolean onKey(int key, int action, int mods) {
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).onKey(key, action, mods)) {
                return true;
            }
        }
        return false;
    }

    public boolean onChar(int codePoint, int modifiers) {
        // Duyệt ngược từ trên xuống dưới (để phần tử nào nổi lên trên cùng sẽ nhận phím trước)
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).onChar(codePoint, modifiers)) {
                return true;
            }
        }
        return false;
    }

    public Rectangle getPosition() {
        return position;
    }

    public void setPosition(Rectangle position) {
        this.position = position;
    }

    public Margin getMargin() {
        return margin;
    }

    public void setMargin(Margin margin) {
        this.margin = margin;
    }

    public UIElement getParent() {
        return parent;
    }
}
