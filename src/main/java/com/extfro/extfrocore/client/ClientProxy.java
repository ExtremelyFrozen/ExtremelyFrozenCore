package com.extfro.extfrocore.client;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.cosmetics.event.RegisterGTCapesEvent;
import com.extfro.extfrocore.api.item.IComponentItem;
import com.extfro.extfrocore.api.item.IGTTool;
import com.extfro.extfrocore.client.model.item.FacadeUnbakedModel;
import com.extfro.extfrocore.client.model.machine.MachineModelLoader;
import com.extfro.extfrocore.client.model.pipe.PipeModel;
import com.extfro.extfrocore.client.model.pipe.PipeModelLoader;
import com.extfro.extfrocore.client.particle.HazardParticle;
import com.extfro.extfrocore.client.particle.MufflerParticle;
import com.extfro.extfrocore.client.renderer.block.MaterialBlockRenderer;
import com.extfro.extfrocore.client.renderer.block.OreBlockRenderer;
import com.extfro.extfrocore.client.renderer.block.SurfaceRockRenderer;
import com.extfro.extfrocore.client.renderer.entity.GTExplosiveRenderer;
import com.extfro.extfrocore.client.renderer.item.ArmorItemRenderer;
import com.extfro.extfrocore.client.renderer.item.TagPrefixItemRenderer;
import com.extfro.extfrocore.client.renderer.item.ToolItemRenderer;
import com.extfro.extfrocore.client.renderer.item.decorator.GTComponentItemDecorator;
import com.extfro.extfrocore.client.renderer.item.decorator.GTLampItemOverlayRenderer;
import com.extfro.extfrocore.client.renderer.item.decorator.GTTankItemFluidPreview;
import com.extfro.extfrocore.client.renderer.item.decorator.GTToolBarRenderer;
import com.extfro.extfrocore.client.renderer.machine.DynamicRenderManager;
import com.extfro.extfrocore.client.renderer.machine.impl.*;
import com.extfro.extfrocore.client.renderer.machine.impl.BoilerMultiPartRender;
import com.extfro.extfrocore.common.CommonEventListener;
import com.extfro.extfrocore.common.data.GTEntityTypes;
import com.extfro.extfrocore.common.data.GTFluids;
import com.extfro.extfrocore.common.data.GTMaterialBlocks;
import com.extfro.extfrocore.common.data.GTMenuTypes;
import com.extfro.extfrocore.common.data.GTParticleTypes;
import com.extfro.extfrocore.common.data.models.GTModels;
import com.extfro.extfrocore.common.item.DrumMachineItem;
import com.extfro.extfrocore.common.item.LampBlockItem;
import com.extfro.extfrocore.common.item.QuantumTankMachineItem;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.data.model.builder.PipeModelBuilder;
import com.extfro.extfrocore.data.pack.event.RegisterDynamicResourcesEvent;
import com.extfro.extfrocore.integration.kjs.GregTechKubeJSPlugin;
import com.extfro.extfrocore.integration.map.ClientCacheManager;
import com.extfro.extfrocore.integration.map.cache.client.GTClientCache;
import com.extfro.extfrocore.integration.map.ftbchunks.FTBChunksPlugin;
import com.extfro.extfrocore.integration.map.layer.Layers;
import com.extfro.extfrocore.integration.map.layer.builtin.FluidRenderLayer;
import com.extfro.extfrocore.integration.map.layer.builtin.OreRenderLayer;
import com.extfro.extfrocore.utils.data.RuntimeBlockstateProvider;
import com.extfro.extfrocore.utils.input.SyncedKeyMapping;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import org.jetbrains.annotations.NotNull;

public class ClientProxy {

    public static void init(IEventBus modBus) {
        modBus.register(ClientProxy.class);
        if (!ExtForCore.isDataGen()) {
            ClientCacheManager.registerClientCache(GTClientCache.instance, "gtceu");
            Layers.registerLayer(OreRenderLayer::new, "ore_veins");
            Layers.registerLayer(FluidRenderLayer::new, "bedrock_fluids");
            CommonEventListener.registerCapes(new RegisterGTCapesEvent());
        }
        initializeDynamicRenders();
    }

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GTEntityTypes.DYNAMITE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(GTEntityTypes.POWDERBARREL.get(), GTExplosiveRenderer::new);
        event.registerEntityRenderer(GTEntityTypes.INDUSTRIAL_TNT.get(), GTExplosiveRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof IComponentItem) {
                event.register(item, GTComponentItemDecorator.INSTANCE);
            }
            if (item instanceof IGTTool) {
                event.register(item, GTToolBarRenderer.INSTANCE);
            }
            if (item instanceof LampBlockItem) {
                event.register(item, GTLampItemOverlayRenderer.INSTANCE);
            }
            if (item instanceof DrumMachineItem) {
                event.register(item, GTTankItemFluidPreview.DRUM);
            }
            if (item instanceof QuantumTankMachineItem) {
                event.register(item, GTTankItemFluidPreview.QUANTUM_TANK);
            }
        }
    }

    @SubscribeEvent
    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        SyncedKeyMapping.onRegisterKeyBinds(event);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ExtForCore.id("hud"), new HudGuiOverlay());
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(GTMenuTypes.COVER_UI.get(), ModularUIContainerScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(GTParticleTypes.HAZARD_PARTICLE.get(), HazardParticle.Provider::new);
        event.registerSpriteSet(GTParticleTypes.MUFFLER_PARTICLE.get(), MufflerParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (ConfigHolder.INSTANCE.compat.minimap.toggle.ftbChunksIntegration &&
                ExtForCore.isModLoaded(EFValues.MODID_FTB_CHUNKS)) {
            FTBChunksPlugin.addEventListeners();
        }
    }

    public static void initializeDynamicRenders() {
        DynamicRenderManager.register(ExtForCore.id("quantum_tank_fluid"), QuantumTankFluidRender.TYPE);
        DynamicRenderManager.register(ExtForCore.id("quantum_chest_item"), QuantumChestItemRender.TYPE);

        DynamicRenderManager.register(ExtForCore.id("fusion_ring"), FusionRingRender.TYPE);
        DynamicRenderManager.register(ExtForCore.id("boiler_multi_parts"), BoilerMultiPartRender.TYPE);

        DynamicRenderManager.register(ExtForCore.id("fluid_area"), FluidAreaRender.TYPE);
        DynamicRenderManager.register(ExtForCore.id("growing_plant"), GrowingPlantRender.TYPE);

        DynamicRenderManager.register(ExtForCore.id("central_monitor"), CentralMonitorRender.TYPE);
    }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(MachineModelLoader.ID, MachineModelLoader.INSTANCE);
        event.register(PipeModelLoader.ID, PipeModelLoader.INSTANCE);
        event.register(ExtForCore.id("facade"), FacadeUnbakedModel.Loader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {

            private static final ResourceLocation TEXTURE = ExtForCore.id("block/fluids/fluid.potion");

            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return TEXTURE;
            }

            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return TEXTURE;
            }

            @Override
            public int getTintColor(@NotNull FluidStack stack) {
                return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                        .getColor() | 0xff000000;
            }
        }, GTFluids.POTION.getType());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preRegisterDynamicAssets(RegisterDynamicResourcesEvent event) {
        PipeModel.DYNAMIC_MODELS.clear();
    }

    @SubscribeEvent
    public static void registerDynamicAssets(RegisterDynamicResourcesEvent event) {
        // regenerate all pipe models in case their textures changed
        // cables may do this, others too if something's removed
        for (var block : GTMaterialBlocks.CABLE_BLOCKS.values()) {
            if (block == null) continue;
            block.get().createPipeModel(RuntimeBlockstateProvider.INSTANCE).dynamicModel();
        }
        for (var block : GTMaterialBlocks.FLUID_PIPE_BLOCKS.values()) {
            if (block == null) continue;
            block.get().createPipeModel(RuntimeBlockstateProvider.INSTANCE).dynamicModel();
        }
        for (var block : GTMaterialBlocks.ITEM_PIPE_BLOCKS.values()) {
            if (block == null) continue;
            block.get().createPipeModel(RuntimeBlockstateProvider.INSTANCE).dynamicModel();
        }

        MaterialBlockRenderer.reinitModels();
        TagPrefixItemRenderer.reinitModels();
        OreBlockRenderer.reinitModels();
        ToolItemRenderer.reinitModels();
        ArmorItemRenderer.reinitModels();
        SurfaceRockRenderer.reinitModels();
        GTModels.registerMaterialFluidModels();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void postRegisterDynamicAssets(RegisterDynamicResourcesEvent event) {
        // do this last so addons can easily add new variants to the registered model set
        PipeModel.initDynamicModels();

        if (ExtForCore.Mods.isKubeJSLoaded()) {
            GregTechKubeJSPlugin.generateMachineBlockModels();
        }
        RuntimeBlockstateProvider.INSTANCE.run();
        PipeModelBuilder.clearRestrictorModelCache();
    }
}
