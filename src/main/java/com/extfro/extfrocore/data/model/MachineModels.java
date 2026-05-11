package com.extfro.extfrocore.data.model;

import com.extfro.extfrocore.api.registry.registrate.MachineBuilder;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;

import org.jetbrains.annotations.Nullable;

public final class MachineModels {

    public static final String OVERLAY_PREFIX = "overlay_";
    public static final String EMISSIVE_SUFFIX = "_emissive";

    public static MachineBuilder.ModelInitializer createBasicMachineModel(ResourceLocation baseModel) {
        return (context, provider, builder) -> {
            var model = provider.models().getExistingFile(baseModel);
            builder.forAllStatesModels(state -> model);
        };
    }

    public static MachineBuilder.ModelInitializer createBasicReplaceableTextureMachineModel(ResourceLocation baseModel,
                                                                                             String... textureNames) {
        return createBasicMachineModel(baseModel).andThen(builder -> builder.addReplaceableTextures(textureNames));
    }

    public static MachineBuilder.ModelInitializer createOverlayCasingMachineModel(ResourceLocation casingTexture,
                                                                                   ResourceLocation overlayModel) {
        return (context, provider, builder) -> {
            BlockModelBuilder model = provider.models().nested()
                    .parent(provider.models().getExistingFile(overlayModel))
                    .texture("all", casingTexture);
            builder.forAllStatesModels(state -> model);
            builder.addReplaceableTextures("all");
        };
    }

    public static MachineBuilder.ModelInitializer createSidedOverlayCasingMachineModel(ResourceLocation casingTexture,
                                                                                        ResourceLocation overlayModel) {
        return (context, provider, builder) -> {
            BlockModelBuilder model = provider.models().nested()
                    .parent(provider.models().getExistingFile(overlayModel));
            sidedCasingTextures(model, casingTexture);
            builder.forAllStatesModels(state -> model);
            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createColorOverlayMachineModel(ResourceLocation parentModel,
                                                                                  ResourceLocation overlay,
                                                                                  @Nullable ResourceLocation pipeOverlay,
                                                                                  @Nullable ResourceLocation emissiveOverlay) {
        return (context, provider, builder) -> {
            BlockModelBuilder model = colorOverlayModel(parentModel, overlay, pipeOverlay, emissiveOverlay,
                    provider.models());
            builder.forAllStatesModels(state -> model);
        };
    }

    public static BlockModelBuilder colorOverlayModel(ResourceLocation parentModel, ResourceLocation overlay,
                                                       @Nullable ResourceLocation pipeOverlay,
                                                       @Nullable ResourceLocation emissiveOverlay,
                                                       BlockModelProvider models) {
        BlockModelBuilder model = models.nested()
                .parent(models.getExistingFile(parentModel))
                .texture("overlay", overlay);
        if (emissiveOverlay != null) {
            model.texture("overlay_emissive", emissiveOverlay);
        }
        if (pipeOverlay != null) {
            model.texture("overlay_pipe", pipeOverlay);
        }
        return model;
    }

    public static void casingTexture(BlockModelBuilder model, String key, ResourceLocation texturePath) {
        model.texture(key, texturePath.withSuffix(key));
    }

    public static BlockModelBuilder sidedCasingTextures(BlockModelBuilder model, ResourceLocation texturePath) {
        if (!texturePath.getPath().endsWith("/")) {
            texturePath = texturePath.withSuffix("/");
        }
        casingTexture(model, "bottom", texturePath);
        casingTexture(model, "top", texturePath);
        casingTexture(model, "side", texturePath);
        return model;
    }

    private MachineModels() {
    }
}
