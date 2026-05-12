package com.extfro.extfrocore.common.cover.detector;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.data.recipe.CustomTags;
import com.extfro.extfrocore.utils.GTTransferUtils;
import com.extfro.extfrocore.utils.RedstoneUtil;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

public class ItemDetectorCover extends DetectorCover {

    public ItemDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && getItemHandler() != null;
    }

    @Override
    protected void update() {
        if (!shouldUpdate())
            return;

        IItemHandler handler = getItemHandler();
        if (handler == null)
            return;

        int storedItems = 0;
        int itemCapacity = handler.getSlots() * handler.getSlotLimit(0);

        if (itemCapacity == 0)
            return;

        for (int i = 0; i < handler.getSlots(); i++) {
            if (handler.getStackInSlot(i).is(CustomTags.SKIP_ITEM_DETECTOR)) continue;
            storedItems += handler.getStackInSlot(i).getCount();
        }

        setRedstoneSignalOutput(RedstoneUtil.computeRedstoneValue(storedItems, itemCapacity, isInverted()));
    }

    @Nullable
    protected IItemHandler getItemHandler() {
        return GTTransferUtils.getItemHandler(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide)
                .orElse(null);
    }
}
