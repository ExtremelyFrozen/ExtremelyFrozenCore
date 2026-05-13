package com.extfro.extfrocore.api.gui.widget;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.IFancyConfigurator;
import com.extfro.extfrocore.common.data.GTItems;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import org.jetbrains.annotations.Nullable;

public class CoverConfigurator implements IFancyConfigurator {

    protected final ICoverable coverable;
    @Nullable
    protected final Direction side;
    @Nullable
    protected final CoverBehavior coverBehavior;

    public CoverConfigurator(ICoverable coverable, @Nullable Direction side, @Nullable CoverBehavior coverBehavior) {
        this.coverable = coverable;
        this.side = side;
        this.coverBehavior = coverBehavior;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.cover_setting.title");
    }

    @Override
    public IGuiTexture getIcon() {
        return new ItemStackTexture(GTItems.ITEM_FILTER.get());
    }

    @Override
    public UIElement createConfigurator() {
        UIElement group = new UIElement().layout(layout -> layout.width(128).height(88));
        if (side != null && coverable.getCoverAtSide(side) instanceof IUICover iuiCover) {
            UIElement coverConfigurator = iuiCover.createUIElement();
            coverConfigurator.layout(layout -> layout.left(4).top(4));
            coverConfigurator.style(style -> style.background(GuiTextures.BACKGROUND));
            group.addChild(coverConfigurator);
            group.layout(layout -> layout.width(Math.max(120, iuiCover.getUIWidth() + 8))
                    .height(Math.max(80, iuiCover.getUIHeight() + 8)));
        }
        return group;
    }
}
