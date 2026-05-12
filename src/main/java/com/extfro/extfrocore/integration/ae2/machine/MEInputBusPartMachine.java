package com.extfro.extfrocore.integration.ae2.machine;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.machine.feature.IDataStickInteractable;
import com.extfro.extfrocore.api.machine.feature.IHasCircuitSlot;
import com.extfro.extfrocore.api.machine.trait.NotifiableItemStackHandler;
import com.extfro.extfrocore.common.data.item.GTDataComponents;
import com.extfro.extfrocore.common.item.behavior.IntCircuitBehaviour;
import com.extfro.extfrocore.integration.ae2.gui.widget.AEItemConfigWidget;
import com.extfro.extfrocore.integration.ae2.gui.AEUIHelper;
import com.extfro.extfrocore.integration.ae2.slot.ExportOnlyAEItemList;
import com.extfro.extfrocore.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.extfro.extfrocore.utils.GTMath;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;

public class MEInputBusPartMachine extends MEBusPartMachine
                                   implements IDataStickInteractable, IHasCircuitSlot {

    protected final static int CONFIG_SIZE = 16;

    protected ExportOnlyAEItemList aeItemHandler;

    public MEInputBusPartMachine(BlockEntityCreationInfo info) {
        super(info, IO.IN);
    }

    /////////////////////////////////
    // ***** Machine LifeCycle ****//
    /////////////////////////////////

    @Override
    public void onMachineDestroyed() {
        flushInventory();
    }

    @Override
    protected NotifiableItemStackHandler createInventory() {
        this.aeItemHandler = new ExportOnlyAEItemList(CONFIG_SIZE);
        return this.aeItemHandler;
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    @Override
    public void autoIO() {
        if (!this.isWorkingEnabled()) return;
        if (!this.shouldSyncME()) return;

        if (this.updateMEStatus()) {
            this.syncME();
            this.updateInventorySubscription();
        }
    }

    protected void syncME() {
        MEStorage networkInv = this.getMainNode().getGrid().getStorageService().getInventory();
        for (ExportOnlyAEItemSlot aeSlot : this.aeItemHandler.getInventory()) {
            // Try to clear the wrong item
            GenericStack exceedItem = aeSlot.exceedStack();
            if (exceedItem != null) {
                long total = exceedItem.amount();
                long inserted = networkInv.insert(exceedItem.what(), exceedItem.amount(), Actionable.MODULATE,
                        this.actionSource);
                if (inserted > 0) {
                    aeSlot.extractItem(0, GTMath.saturatedCast(inserted), false);
                    continue;
                } else {
                    aeSlot.extractItem(0, GTMath.saturatedCast(total), false);
                }
            }
            // Fill it
            GenericStack reqItem = aeSlot.requestStack();
            if (reqItem != null) {
                long extracted = networkInv.extract(reqItem.what(), reqItem.amount(), Actionable.MODULATE,
                        this.actionSource);
                if (extracted != 0) {
                    aeSlot.addStack(new GenericStack(reqItem.what(), extracted));
                }
            }
        }
    }

    protected void flushInventory() {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            for (var aeSlot : aeItemHandler.getInventory()) {
                GenericStack stock = aeSlot.getStock();
                if (stock != null) {
                    grid.getStorageService().getInventory().insert(stock.what(), stock.amount(), Actionable.MODULATE,
                            actionSource);
                }
            }
        }
    }

    ///////////////////////////////
    // ********** GUI ***********//
    ///////////////////////////////

    @Override
    public UIElement createUIWidget() {
        UIElement group = AEUIHelper.group(0, 0, 170, 82);
        group.addChild(AEUIHelper.label(3, 0, () -> this.isOnline ?
                Component.translatable("gtceu.gui.me_network.online") :
                Component.translatable("gtceu.gui.me_network.offline")));
        group.addChild(new AEItemConfigWidget(3, 10, this.aeItemHandler));
        return group;
    }

    ////////////////////////////////
    // ******* Interaction *******//
    ////////////////////////////////

    @Override
    public final InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (!isRemote()) {
            CompoundTag tag = new CompoundTag();
            tag.put("MEInputBus", writeConfigToTag(player.registryAccess()));
            dataStick.set(GTDataComponents.DATA_COPY_TAG, CustomData.of(tag));
            dataStick.set(DataComponents.CUSTOM_NAME,
                    Component.translatable("gtceu.machine.me.item_import.data_stick.name"));
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_copy_settings"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public final InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        CustomData tag = dataStick.get(GTDataComponents.DATA_COPY_TAG);
        if (tag == null || !tag.contains("MEInputBus")) {
            return InteractionResult.PASS;
        }

        if (!isRemote()) {
            readConfigFromTag(player.registryAccess(), tag.copyTag().getCompound("MEInputBus"));
            this.updateInventorySubscription();
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_paste_settings"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    ////////////////////////////////
    // ****** Configuration ******//
    ////////////////////////////////

    protected CompoundTag writeConfigToTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag configStacks = new CompoundTag();
        tag.put("ConfigStacks", configStacks);
        for (int i = 0; i < CONFIG_SIZE; i++) {
            var slot = this.aeItemHandler.getInventory()[i];
            GenericStack config = slot.getConfig();
            if (config == null) {
                continue;
            }
            CompoundTag stackTag = GenericStack.writeTag(provider, config);
            configStacks.put(Integer.toString(i), stackTag);
        }
        tag.putByte("GhostCircuit",
                (byte) IntCircuitBehaviour.getCircuitConfiguration(circuitInventory.getStackInSlot(0)));
        tag.putBoolean("DistinctBuses", isDistinct());
        return tag;
    }

    protected void readConfigFromTag(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("ConfigStacks")) {
            CompoundTag configStacks = tag.getCompound("ConfigStacks");
            for (int i = 0; i < CONFIG_SIZE; i++) {
                String key = Integer.toString(i);
                if (configStacks.contains(key)) {
                    CompoundTag configTag = configStacks.getCompound(key);
                    this.aeItemHandler.getInventory()[i].setConfig(GenericStack.readTag(provider, configTag));
                } else {
                    this.aeItemHandler.getInventory()[i].setConfig(null);
                }
            }
        }
        if (tag.contains("GhostCircuit")) {
            circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(tag.getByte("GhostCircuit")));
        }
        if (tag.contains("DistinctBuses")) {
            setDistinct(tag.getBoolean("DistinctBuses"));
        }
    }
}
