package com.extfro.extfrocore.api.item.component;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.lowdragmc.lowdraglib2.gui.factory.HeldItemUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;

public interface IItemUIFactory extends IInteractionItem {

    ModularUI createUI(HeldItemUIMenuType.HeldItemUIHolder holder);

    @Override
    default InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                   InteractionHand usedHand) {
        if (player instanceof ServerPlayer serverPlayer) {
            HeldItemUIMenuType.openUI(serverPlayer, usedHand);
        }
        return InteractionResultHolder.sidedSuccess(item, level.isClientSide());
    }
}
