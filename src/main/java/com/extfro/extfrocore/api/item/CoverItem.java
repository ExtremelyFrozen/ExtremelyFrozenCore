package com.extfro.extfrocore.api.item;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.machine.MetaMachine;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import lombok.Getter;

public class CoverItem extends Item {

    @Getter
    private final CoverDefinition definition;

    public CoverItem(Properties properties, CoverDefinition definition) {
        super(properties);
        this.definition = definition;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        MetaMachine machine = MetaMachine.getMachine(context.getLevel(), context.getClickedPos());
        ICoverable coverable = machine == null ? null : machine.getCoverContainer();
        if (coverable == null || context.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        var coverSide = ICoverable.rayTraceCoverableSide(coverable, context.getPlayer());
        if (coverSide == null || coverable.getCoverAtSide(coverSide) != null ||
                !coverable.canPlaceCoverOnSide(definition, coverSide)) {
            return InteractionResult.PASS;
        }
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            ItemStack itemStack = context.getItemInHand();
            boolean placed = coverable.placeCoverOnSide(coverSide, itemStack, definition, serverPlayer);
            if (placed && !serverPlayer.isCreative()) {
                itemStack.shrink(1);
            }
            return placed ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }
}
