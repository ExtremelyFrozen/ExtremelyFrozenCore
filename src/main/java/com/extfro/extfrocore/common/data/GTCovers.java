package com.extfro.extfrocore.common.data;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFAPI;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.registry.GTRegistries;
import com.extfro.extfrocore.client.renderer.cover.*;
import com.extfro.extfrocore.common.cover.*;
import com.extfro.extfrocore.common.cover.detector.*;
import com.extfro.extfrocore.common.cover.ender.EnderFluidLinkCover;
import com.extfro.extfrocore.common.cover.ender.EnderItemLinkCover;
import com.extfro.extfrocore.common.cover.ender.EnderRedstoneLinkCover;
import com.extfro.extfrocore.common.cover.voiding.AdvancedFluidVoidingCover;
import com.extfro.extfrocore.common.cover.voiding.AdvancedItemVoidingCover;
import com.extfro.extfrocore.common.cover.voiding.FluidVoidingCover;
import com.extfro.extfrocore.common.cover.voiding.ItemVoidingCover;

import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;

public class GTCovers {

    public static final int[] ALL_TIERS = EFValues.tiersBetween(EFValues.LV,
            EFAPI.isHighTier() ? EFValues.OpV : EFValues.UV);
    public static final int[] ALL_TIERS_WITH_ULV = EFValues.tiersBetween(EFValues.ULV,
            EFAPI.isHighTier() ? EFValues.OpV : EFValues.UV);

    public final static CoverDefinition FACADE = register("facade", FacadeCover::new,
            () -> () -> FacadeCoverRenderer.INSTANCE);

    public final static CoverDefinition ITEM_FILTER = register("item_filter", ItemFilterCover::new);
    public final static CoverDefinition FLUID_FILTER = register("fluid_filter", FluidFilterCover::new);

    public final static CoverDefinition INFINITE_WATER = register("infinite_water", InfiniteWaterCover::new);
    public final static CoverDefinition ENDER_FLUID_LINK = register("ender_fluid_link", EnderFluidLinkCover::new);
    public final static CoverDefinition ENDER_ITEM_LINK = register("ender_item_link", EnderItemLinkCover::new);
    public final static CoverDefinition ENDER_REDSTONE_LINK = register("ender_redstone_link",
            EnderRedstoneLinkCover::new);
    public final static CoverDefinition SHUTTER = register("shutter", ShutterCover::new);
    public final static CoverDefinition COVER_STORAGE = register("storage", StorageCover::new);
    public final static CoverDefinition WIRELESS_TRANSMITTER = register("wireless_transmitter",
            WirelessTransmitterCover::new);

    public final static CoverDefinition[] CONVEYORS = registerTiered("conveyor", ConveyorCover::new,
            () -> tier -> new IOCoverRenderer(
                    ExtForCore.id("block/cover/conveyor"),
                    null,
                    ExtForCore.id("block/cover/conveyor_emissive"),
                    ExtForCore.id("block/cover/conveyor_inverted_emissive")),
            ALL_TIERS);

    public final static CoverDefinition[] ROBOT_ARMS = registerTiered("robot_arm", RobotArmCover::new,
            () -> tier -> new IOCoverRenderer(
                    ExtForCore.id("block/cover/arm"),
                    null,
                    ExtForCore.id("block/cover/arm_emissive"),
                    ExtForCore.id("block/cover/arm_inverted_emissive")),
            ALL_TIERS);

    public final static CoverDefinition[] PUMPS = registerTiered("pump", PumpCover::new,
            () -> tier -> IOCoverRenderer.PUMP_LIKE_COVER_RENDERER, ALL_TIERS);

    public final static CoverDefinition[] FLUID_REGULATORS = registerTiered("fluid_regulator", FluidRegulatorCover::new,
            () -> tier -> IOCoverRenderer.PUMP_LIKE_COVER_RENDERER, ALL_TIERS);

    public final static CoverDefinition COMPUTER_MONITOR = register("computer_monitor", ComputerMonitorCover::new);

    public final static CoverDefinition MACHINE_CONTROLLER = register("machine_controller",
            MachineControllerCover::new);

    // Voiding
    public final static CoverDefinition ITEM_VOIDING = register("item_voiding", ItemVoidingCover::new);
    public final static CoverDefinition ITEM_VOIDING_ADVANCED = register("item_voiding_advanced",
            AdvancedItemVoidingCover::new);
    public final static CoverDefinition FLUID_VOIDING = register("fluid_voiding", FluidVoidingCover::new);
    public final static CoverDefinition FLUID_VOIDING_ADVANCED = register("fluid_voiding_advanced",
            AdvancedFluidVoidingCover::new);

    // Detectors
    public final static CoverDefinition ACTIVITY_DETECTOR = register("activity_detector", ActivityDetectorCover::new);
    public final static CoverDefinition ACTIVITY_DETECTOR_ADVANCED = register("activity_detector_advanced",
            AdvancedActivityDetectorCover::new);
    public final static CoverDefinition FLUID_DETECTOR = register("fluid_detector", FluidDetectorCover::new);
    public final static CoverDefinition FLUID_DETECTOR_ADVANCED = register("fluid_detector_advanced",
            AdvancedFluidDetectorCover::new);
    public final static CoverDefinition ITEM_DETECTOR = register("item_detector", ItemDetectorCover::new);
    public final static CoverDefinition ITEM_DETECTOR_ADVANCED = register("item_detector_advanced",
            AdvancedItemDetectorCover::new);
    public final static CoverDefinition ENERGY_DETECTOR = register("energy_detector", EnergyDetectorCover::new);
    public final static CoverDefinition ENERGY_DETECTOR_ADVANCED = register("energy_detector_advanced",
            AdvancedEnergyDetectorCover::new);
    public final static CoverDefinition MAINTENANCE_DETECTOR = register("maintenance_detector",
            MaintenanceDetectorCover::new);

    // Solar Panels
    public final static CoverDefinition SOLAR_PANEL_BASIC = register("solar_panel", CoverSolarPanel::new);
    public final static CoverDefinition[] SOLAR_PANEL = registerTiered("solar_panel", CoverSolarPanel::new,
            () -> tier -> new SimpleCoverRenderer(ExtForCore.id("block/cover/solar_panel")), ALL_TIERS_WITH_ULV);

    ///////////////////////////////////////////////
    // *********** UTIL METHODS ***********//
    ///////////////////////////////////////////////

    private static CoverDefinition register(String id, CoverDefinition.CoverBehaviourProvider behaviorCreator) {
        return register(id, behaviorCreator, () -> () -> new SimpleCoverRenderer(ExtForCore.id("block/cover/" + id)));
    }

    private static CoverDefinition register(String id, CoverDefinition.CoverBehaviourProvider behaviorCreator,
                                            Supplier<Supplier<ICoverRenderer>> coverRenderer) {
        return register(ExtForCore.id(id), behaviorCreator, coverRenderer);
    }

    public static CoverDefinition register(ResourceLocation id, CoverDefinition.CoverBehaviourProvider behaviorCreator,
                                           Supplier<Supplier<ICoverRenderer>> coverRenderer) {
        var definition = new CoverDefinition(id, behaviorCreator, coverRenderer);
        GTRegistries.register(GTRegistries.COVERS, definition.getId(), definition);
        return definition;
    }

    private static CoverDefinition[] registerTiered(String id,
                                                    CoverDefinition.TieredCoverBehaviourProvider behaviorCreator,
                                                    Supplier<Int2ObjectFunction<ICoverRenderer>> coverRenderer,
                                                    int... tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> {
            var name = id + "." + EFValues.VN[tier].toLowerCase(Locale.ROOT);
            return register(name, (def, coverable, side) -> behaviorCreator.create(def, coverable, side, tier),
                    () -> () -> coverRenderer.get().apply(tier));
        }).toArray(CoverDefinition[]::new);
    }

    private static CoverDefinition[] registerTiered(String id,
                                                    CoverDefinition.TieredCoverBehaviourProvider behaviorCreator,
                                                    int... tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> {
            var name = id + "." + EFValues.VN[tier].toLowerCase(Locale.ROOT);
            return register(name, (def, coverable, side) -> behaviorCreator.create(def, coverable, side, tier));
        }).toArray(CoverDefinition[]::new);
    }

    public static void init() {}
}
