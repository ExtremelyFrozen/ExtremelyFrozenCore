package com.extfro.extfrocore.common.machine.multiblock.part;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.machine.multiblock.part.MultiblockPartMachine;
import com.extfro.extfrocore.api.machine.trait.NotifiableComputationContainer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;

public class OpticalComputationHatchMachine extends MultiblockPartMachine {

    @Getter
    private final boolean transmitter;

    protected NotifiableComputationContainer computationContainer;

    public OpticalComputationHatchMachine(BlockEntityCreationInfo info, boolean transmitter) {
        super(info);
        this.transmitter = transmitter;
        this.computationContainer = attachTrait(new NotifiableComputationContainer(IO.IN, transmitter));
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public boolean canShared() {
        return false;
    }
}
