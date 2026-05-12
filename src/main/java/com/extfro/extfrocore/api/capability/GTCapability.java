package com.extfro.extfrocore.api.capability;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.machine.feature.multiblock.IMaintenanceMachine;
import com.extfro.extfrocore.api.machine.trait.RecipeLogic;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

public class GTCapability {

    public static final BlockCapability<IEnergyContainer, Direction> CAPABILITY_ENERGY_CONTAINER = BlockCapability
            .createSided(ExtForCore.id("energy_container"), IEnergyContainer.class);
    public static final BlockCapability<IEnergyInfoProvider, Direction> CAPABILITY_ENERGY_INFO_PROVIDER = BlockCapability
            .createSided(ExtForCore.id("energy_info_provider"), IEnergyInfoProvider.class);
    public static final BlockCapability<ICoverable, Direction> CAPABILITY_COVERABLE = BlockCapability
            .createSided(ExtForCore.id("coverable"), ICoverable.class);
    public static final BlockCapability<IWorkable, Direction> CAPABILITY_WORKABLE = BlockCapability
            .createSided(ExtForCore.id("workable"), IWorkable.class);
    public static final BlockCapability<IControllable, Direction> CAPABILITY_CONTROLLABLE = BlockCapability
            .createSided(ExtForCore.id("controllable"), IControllable.class);
    public static final BlockCapability<RecipeLogic, Direction> CAPABILITY_RECIPE_LOGIC = BlockCapability
            .createSided(ExtForCore.id("recipe_logic"), RecipeLogic.class);
    public static final ItemCapability<IElectricItem, Void> CAPABILITY_ELECTRIC_ITEM = ItemCapability
            .createVoid(ExtForCore.id("electric_item"), IElectricItem.class);
    public static final BlockCapability<IMaintenanceMachine, Direction> CAPABILITY_MAINTENANCE_MACHINE = BlockCapability
            .createSided(ExtForCore.id("maintenance"), IMaintenanceMachine.class);
    public static final BlockCapability<ILaserContainer, Direction> CAPABILITY_LASER = BlockCapability
            .createSided(ExtForCore.id("laser_container"), ILaserContainer.class);
    public static final BlockCapability<IOpticalComputationProvider, Direction> CAPABILITY_COMPUTATION_PROVIDER = BlockCapability
            .createSided(ExtForCore.id("computation_provider"), IOpticalComputationProvider.class);
    public static final BlockCapability<IDataAccessHatch, Direction> CAPABILITY_DATA_ACCESS = BlockCapability
            .createSided(ExtForCore.id("data_access"), IDataAccessHatch.class);
    public static final BlockCapability<IHazardParticleContainer, Direction> CAPABILITY_HAZARD_CONTAINER = BlockCapability
            .createSided(ExtForCore.id("hazard_particle_container"), IHazardParticleContainer.class);
    public static final BlockCapability<IMonitorComponent, Direction> CAPABILITY_MONITOR_COMPONENT = BlockCapability
            .createSided(ExtForCore.id("monitor_component"), IMonitorComponent.class);
}
