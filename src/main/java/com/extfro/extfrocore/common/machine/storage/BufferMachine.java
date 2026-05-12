package com.extfro.extfrocore.common.machine.storage;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.widget.SlotWidget;
import com.extfro.extfrocore.api.gui.widget.TankWidget;
import com.extfro.extfrocore.api.machine.TieredMachine;
import com.extfro.extfrocore.api.machine.feature.IFancyUIMachine;
import com.extfro.extfrocore.api.machine.trait.AutoOutputTrait;
import com.extfro.extfrocore.api.machine.trait.NotifiableFluidTank;
import com.extfro.extfrocore.api.machine.trait.NotifiableItemStackHandler;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.common.machine.gui.MachineUIHelper;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import lombok.Getter;

import java.util.List;

public class BufferMachine extends TieredMachine implements IFancyUIMachine {

    public static final int TANK_SIZE = 64000;

    @SaveField
    @Getter
    protected final NotifiableItemStackHandler inventory;

    @SaveField
    @Getter
    protected final NotifiableFluidTank tank;
    @SaveField
    @SyncToClient

    public final AutoOutputTrait autoOutput;

    public BufferMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        this.inventory = attachTrait(new NotifiableItemStackHandler(getInventorySize(tier), IO.BOTH));
        this.tank = attachTrait(new NotifiableFluidTank(getTankSize(tier), TANK_SIZE, IO.BOTH));
        this.autoOutput = attachTrait(new AutoOutputTrait(List.of(inventory), List.of(tank)));
    }

    ////////////////////////////////
    // ***** Initialization ******//
    ////////////////////////////////

    public static int getInventorySize(int tier) {
        return (int) Math.pow(tier + 2, 2);
    }

    public static int getTankSize(int tier) {
        return tier + 2;
    }

    ////////////////////////////////
    // ********** GUI *********** //
    ////////////////////////////////

    @Override
    public UIElement createUIWidget() {
        int invTier = getTankSize(tier);
        var group = MachineUIHelper.group(18 * (invTier + 1) + 16, 18 * invTier + 16);
        var container = MachineUIHelper.group(4, 4, 18 * (invTier + 1) + 8, 18 * invTier + 8)
                .style(style -> style.background(GuiTextures.BACKGROUND_INVERSE));

        int index = 0;
        for (int y = 0; y < invTier; y++) {
            for (int x = 0; x < invTier; x++) {
                container.addChild(new SlotWidget(
                        getInventory().storage, index++, 4 + x * 18, 4 + y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }

        index = 0;
        for (int y = 0; y < invTier; y++) {
            container.addChild(new TankWidget(
                    tank.getStorages()[index++], 4 + invTier * 18, 4 + y * 18, true, true)
                    .setBackground(GuiTextures.FLUID_SLOT));
        }

        group.addChild(container);
        return group;
    }
}
