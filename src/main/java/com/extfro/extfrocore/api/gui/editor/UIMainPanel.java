package com.extfro.extfrocore.api.gui.editor;

import com.extfro.extfrocore.api.gui.GuiTextures;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.gui.editor.ui.MainPanel;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;

public class UIMainPanel extends MainPanel {

    final String description;

    public UIMainPanel(Editor editor, UIElement root, String description) {
        super(editor, root);
        this.setBackground(new IGuiTexture() {

            @Override
            @OnlyIn(Dist.CLIENT)
            public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
                if (description != null) {
                    new TextTexture(description).scale(2.0f).draw(graphics, mouseX, mouseY, x, y,
                            width - editor.getConfigPanel().getSize().getWidth(), height);
                }
                var border = 4;
                var background = GuiTextures.BACKGROUND;
                var w = Math.max(root.getSizeWidth() + border * 2, 172);
                var h = Math.max(root.getSizeHeight() + border * 2, 86);
                background.draw(graphics, mouseX, mouseY,
                        root.getPositionX() - (w - root.getSizeWidth()) / 2f,
                        root.getPositionY() - (h - root.getSizeHeight()) / 2f,
                        w, h);
            }
        });
        this.description = description;
    }
}
