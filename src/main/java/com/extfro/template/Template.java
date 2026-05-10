package com.extfro.template;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;

import com.extfro.template.common.CommonProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@Mod(Template.MOD_ID)
public class Template {

    public static final String MOD_ID = "template";
    public static final String MOD_NAME = "Template";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @ApiStatus.Internal
    public static IEventBus tenModBus;

    public Template(IEventBus modBus, FMLModContainer container) {
        Template.tenModBus = modBus;
        CommonProxy.init(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
