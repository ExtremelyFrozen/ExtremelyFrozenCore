package com.extfro.extfrocore;

import com.extfro.extfrocore.common.CommonProxy;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@Mod(ExtForCore.MOD_ID)
public class ExtForCore {

    public static final String MOD_ID = "extfrocore";
    public static final String MOD_NAME = "ExtremelyFrozenCore";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @ApiStatus.Internal
    public static IEventBus tenModBus;

    public ExtForCore(IEventBus modBus, FMLModContainer container) {
        ExtForCore.tenModBus = modBus;
        CommonProxy.init(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
