package com.extfro.extfrocore.api.machine.steam;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.feature.ITieredMachine;
import com.extfro.extfrocore.api.machine.property.GTMachineModelProperties;
import com.extfro.extfrocore.api.machine.trait.NotifiableFluidTank;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.common.data.GTMaterials;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.fluids.FluidType;

import lombok.Getter;

import java.util.function.Function;

/**
 * A singleblock machine with a steam tank.
 */
public abstract class SteamMachine extends MetaMachine implements ITieredMachine {

    public static final BooleanProperty STEEL_PROPERTY = GTMachineModelProperties.IS_STEEL_MACHINE;

    @Getter
    public final boolean isHighPressure;
    @SaveField
    public final NotifiableFluidTank steamTank;

    public SteamMachine(BlockEntityCreationInfo info, boolean isHighPressure,
                        NotifiableFluidTank steamTank) {
        super(info);
        this.isHighPressure = isHighPressure;
        this.steamTank = attachTrait(steamTank);
        this.steamTank.setFilter(f -> f.getFluid().is(GTMaterials.Steam.getFluidTag()));
    }

    public SteamMachine(BlockEntityCreationInfo info, boolean isHighPressure,
                        Function<SteamMachine, NotifiableFluidTank> steamTankFactory) {
        super(info);
        this.isHighPressure = isHighPressure;
        this.steamTank = steamTankFactory.apply(this);
        this.steamTank.setFilter(f -> f.getFluid().is(GTMaterials.Steam.getFluidTag()));
    }

    public SteamMachine(BlockEntityCreationInfo info, boolean isHighPressure) {
        this(info, isHighPressure, new NotifiableFluidTank(1, 16 * FluidType.BUCKET_VOLUME, IO.IN));
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////

    @Override
    public int getTier() {
        return isHighPressure ? 1 : 0;
    }
}
