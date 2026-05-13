package com.extfro.extfrocore.api;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.addon.AddonFinder;
import com.extfro.extfrocore.api.addon.IGTAddon;
import com.extfro.extfrocore.api.block.ICoilType;
import com.extfro.extfrocore.api.block.IFilterType;
import com.extfro.extfrocore.api.data.chemical.material.IMaterialRegistry;
import com.extfro.extfrocore.api.machine.multiblock.IBatteryData;
import com.extfro.extfrocore.common.block.BatteryBlock;
import com.extfro.extfrocore.common.block.CoilBlock;
import com.extfro.extfrocore.config.ConfigHolder;

import net.minecraft.world.level.block.Block;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EFAPI {

    public static final int GT_DATA_VERSION = 4;
    public static final String NETWORK_VERSION = "4";

    /** Will always be available */
    public static ExtForCore instance;
    /** Will be available at the Construction stage */
    public static IMaterialRegistry materialManager;

    /** Will be available at the Pre-Initialization stage */
    @Getter
    private static boolean highTier;
    private static boolean highTierInitialized;

    public static final Map<ICoilType, Supplier<CoilBlock>> HEATING_COILS = new HashMap<>();
    public static final Map<IFilterType, Supplier<Block>> CLEANROOM_FILTERS = new HashMap<>();
    public static final Map<IBatteryData, Supplier<BatteryBlock>> PSS_BATTERIES = new HashMap<>();

    /**
     * Initializes High-Tier. Internal use only, do not attempt to call this.
     */
    @ApiStatus.Internal
    public static void initializeHighTier() {
        if (highTierInitialized) throw new IllegalStateException("High-Tier is already initialized.");
        highTier = ConfigHolder.INSTANCE.machines.highTierContent ||
                AddonFinder.getAddonList().stream().anyMatch(IGTAddon::requiresHighTier) || ExtForCore.isDev();
        highTierInitialized = true;

        if (isHighTier()) ExtForCore.LOGGER.info("High-Tier is Enabled.");
        else ExtForCore.LOGGER.info("High-Tier is Disabled.");
    }
}
