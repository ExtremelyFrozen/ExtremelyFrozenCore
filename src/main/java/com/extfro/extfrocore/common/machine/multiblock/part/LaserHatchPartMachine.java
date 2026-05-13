package com.extfro.extfrocore.common.machine.multiblock.part;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.machine.feature.IDataInfoProvider;
import com.extfro.extfrocore.api.machine.multiblock.part.TieredIOPartMachine;
import com.extfro.extfrocore.api.machine.trait.NotifiableLaserContainer;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.common.item.behavior.PortableScannerBehavior;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LaserHatchPartMachine extends TieredIOPartMachine implements IDataInfoProvider {

    @SaveField
    private NotifiableLaserContainer buffer;

    public LaserHatchPartMachine(BlockEntityCreationInfo info, IO io, int tier, int amperage) {
        super(info, tier, io);
        if (io == IO.OUT) {
            this.buffer = attachTrait(NotifiableLaserContainer.emitterContainer(EFValues.V[tier] * 64L * amperage,
                    EFValues.V[tier], amperage));
            this.buffer.setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            this.buffer = attachTrait(NotifiableLaserContainer.receiverContainer(EFValues.V[tier] * 64L * amperage,
                    EFValues.V[tier], amperage));
            this.buffer.setSideInputCondition(s -> s == getFrontFacing());
        }
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @NotNull
    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_ELECTRICAL_INFO) {
            return Collections.singletonList(Component.translatable(
                    String.format("%d/%d EU", buffer.getEnergyStored(), buffer.getEnergyCapacity())));
        }
        return new ArrayList<>();
    }

    //////////////////////////////////////
    // ********** Misc **********//
    //////////////////////////////////////

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return EFValues.VC[getTier()];
        }
        return super.tintColor(index);
    }
}
