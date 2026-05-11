package com.extfro.extfrocore.api.gui.widget;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.IUICover;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import org.jetbrains.annotations.Nullable;

public class CoverConfigurator {

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

    public Component getTitle() {
        if (coverBehavior instanceof IUICover uiCover) {
            return uiCover.getUITitle();
        }
        return Component.translatable("extfrocore.gui.cover_settings.title");
    }

    public boolean canConfigure() {
        return getUICover() != null;
    }

    public UIElement createConfigurator() {
        return createConfigurator(null);
    }

    public UIElement createConfigurator(@Nullable Player player) {
        UIElement group = new UIElement().layout(layout -> {
            layout.width(128);
            layout.height(88);
        });
        IUICover uiCover = getUICover();
        if (uiCover == null) {
            return group;
        }

        UIElement coverElement = uiCover.createUIElement(player);
        group.addChild(coverElement);
        return group;
    }

    @Nullable
    protected IUICover getUICover() {
        if (side == null) {
            return null;
        }
        CoverBehavior cover = coverable.getCoverAtSide(side);
        if (cover == null) {
            cover = coverBehavior;
        }
        return cover instanceof IUICover uiCover ? uiCover : null;
    }
}
