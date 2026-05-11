package com.extfro.extfrocore.api.cover;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import org.jetbrains.annotations.Nullable;

public interface IUICover {

    default CoverBehavior self() {
        return (CoverBehavior) this;
    }

    default boolean isInvalid() {
        return self().coverHolder.isRemoved() || self().coverHolder.getCoverAtSide(self().attachedSide) != self();
    }

    default boolean isRemote() {
        return self().coverHolder.isRemote();
    }

    default Component getUITitle() {
        return Component.translatable("extfrocore.gui.cover_settings.title");
    }

    default UIElement createUIElement(@Nullable Player player) {
        return createUIElement();
    }

    default UIElement createUIElement() {
        return new UIElement().layout(layout -> {
            layout.width(120);
            layout.height(80);
        });
    }

    default void onUIClosed() {}
}
