package com.extfro.extfrocore.api.gui.factory;

import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.common.data.GTMenuTypes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib2.gui.factory.IContainerUIHolder;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;

public class CoverUIFactory {

    public static final CoverUIFactory INSTANCE = new CoverUIFactory();

    public boolean openUI(CoverBehavior cover, ServerPlayer player) {
        if (!(cover instanceof IUICover)) {
            return false;
        }
        var holder = new CoverUIHolder(cover, player, cover.coverHolder.getBlockPos(), cover.attachedSide);
        return player.openMenu(holder).isPresent();
    }

    public static ModularUIContainerMenu create(int id, Inventory inventory, RegistryFriendlyByteBuf syncData) {
        var holder = readHolderFromSyncData(inventory.player, syncData);
        if (holder == null) {
            throw new IllegalArgumentException("Unable to create cover UI from sync data.");
        }
        return new ModularUIContainerMenu(GTMenuTypes.COVER_UI.get(), id, inventory, holder);
    }

    @OnlyIn(Dist.CLIENT)
    private static CoverUIHolder readHolderFromSyncData(Player player, RegistryFriendlyByteBuf syncData) {
        Level world = Minecraft.getInstance().level;
        if (world == null) return null;
        var pos = syncData.readBlockPos();
        var side = syncData.readEnum(Direction.class);
        var coverable = GTCapabilityHelper.getCoverable(world, pos, side);
        if (coverable != null) {
            var cover = coverable.getCoverAtSide(side);
            if (cover instanceof IUICover) {
                return new CoverUIHolder(cover, player, pos, side);
            }
        }
        return null;
    }

    public record CoverUIHolder(CoverBehavior cover, Player player, BlockPos pos, Direction side)
            implements MenuProvider, IContainerUIHolder {

        @Override
        public Component getDisplayName() {
            return Component.translatable(cover.getAttachItem().getDescriptionId());
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return new ModularUIContainerMenu(GTMenuTypes.COVER_UI.get(), id, inventory, this);
        }

        @Override
        public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
            buffer.writeBlockPos(pos);
            buffer.writeEnum(side);
        }

        @Override
        public ModularUI createUI(Player player) {
            return ((IUICover) cover).createUI(player);
        }

        @Override
        public boolean isStillValid(Player player) {
            return cover instanceof IUICover uiCover && !uiCover.isInvalid();
        }
    }
}
