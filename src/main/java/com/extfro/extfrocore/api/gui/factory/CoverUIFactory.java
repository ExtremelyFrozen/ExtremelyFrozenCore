package com.extfro.extfrocore.api.gui.factory;

import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.IUICover;

import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public final class CoverUIFactory {

    public static final CoverUIFactory INSTANCE = new CoverUIFactory();

    private CoverUIOpener opener = CoverUIOpener.NO_OP;

    private CoverUIFactory() {}

    public void setOpener(CoverUIOpener opener) {
        this.opener = Objects.requireNonNull(opener, "opener");
    }

    public boolean openUI(CoverBehavior coverBehavior, ServerPlayer player) {
        if (!(coverBehavior instanceof IUICover uiCover) || uiCover.isInvalid()) {
            return false;
        }
        return opener.open(coverBehavior, uiCover, player);
    }

    @FunctionalInterface
    public interface CoverUIOpener {

        CoverUIOpener NO_OP = (coverBehavior, uiCover, player) -> false;

        boolean open(CoverBehavior coverBehavior, IUICover uiCover, ServerPlayer player);
    }
}
