package com.extfro.extfrocore.api.gui;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;

/**
 * Temporary bridge for legacy GTCEu-style ModularUI builder call sites.
 */
public class ModularUIBuilder extends ModularUI {

    private final UIElement root;

    public ModularUIBuilder(int width, int height, Object holder, Player player) {
        this(createRoot(width, height), player);
    }

    private ModularUIBuilder(UIElement root, Player player) {
        super(UI.of(root), player);
        this.root = root;
    }

    private static UIElement createRoot(int width, int height) {
        return new UIElement().layout(layout -> layout.width(width).height(height));
    }

    public ModularUIBuilder background(IGuiTexture texture) {
        root.style(style -> style.background(texture));
        return this;
    }

    public ModularUIBuilder widget(UIElement element) {
        root.addChild(element);
        return this;
    }

    public UIElement root() {
        return root;
    }
}
