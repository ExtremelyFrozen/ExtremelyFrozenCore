package com.extfro.extfrocore.data.material;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.material.info.EFMaterialIconSet;
import com.extfro.extfrocore.api.material.info.EFMaterialIconType;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EFMaterialIconModelProvider implements DataProvider {

    private final PackOutput.PathProvider itemModelPathProvider;
    private final PackOutput.PathProvider blockModelPathProvider;
    private final ExistingFileHelper existingFileHelper;

    public EFMaterialIconModelProvider(PackOutput output,
                                       CompletableFuture<?> registries,
                                       ExistingFileHelper existingFileHelper) {
        this.itemModelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
        this.blockModelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (EFMaterialIconSet iconSet : EFMaterialIconSet.ICON_SETS.values()) {
            for (EFMaterialIconType iconType : EFMaterialIconType.ICON_TYPES.values()) {
                saveItemModel(cache, futures, iconSet, iconType);
                saveBlockModel(cache, futures, iconSet, iconType);
            }
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void saveItemModel(CachedOutput cache, List<CompletableFuture<?>> futures,
                               EFMaterialIconSet iconSet, EFMaterialIconType iconType) {
        ResourceLocation texture = firstExisting(iconType.getItemTextureCandidates(iconSet));
        if (texture == null) {
            return;
        }

        JsonObject model = EFMaterialModelTemplates.generatedItem(texture);
        ResourceLocation secondary = firstExisting(iconType.getItemTextureCandidates(iconSet, "secondary"));
        if (secondary != null) {
            model.getAsJsonObject("textures").addProperty("layer1", secondary.toString());
        }
        ResourceLocation overlay = firstExisting(iconType.getItemTextureCandidates(iconSet, "overlay"));
        if (overlay != null) {
            model.getAsJsonObject("textures").addProperty("layer2", overlay.toString());
        }
        saveStable(cache, futures, model, itemModelPathProvider.json(iconType.getItemModelPath(iconSet, false)));
    }

    private void saveBlockModel(CachedOutput cache, List<CompletableFuture<?>> futures,
                                EFMaterialIconSet iconSet, EFMaterialIconType iconType) {
        ResourceLocation texture = firstExisting(iconType.getBlockTextureCandidates(iconSet));
        if (texture == null) {
            return;
        }

        JsonObject model = EFMaterialModelTemplates.tintedCubeAll(texture);
        ResourceLocation secondary = firstExisting(iconType.getBlockTextureCandidates(iconSet, "secondary"));
        if (secondary != null) {
            model = EFMaterialModelTemplates.tintedCubeAllWithSecondary(texture, secondary);
        }
        saveStable(cache, futures, model, blockModelPathProvider.json(iconType.getBlockModelPath(iconSet, false)));
    }

    private ResourceLocation firstExisting(List<ResourceLocation> candidates) {
        for (ResourceLocation candidate : candidates) {
            if (existingFileHelper.exists(candidate, PackType.CLIENT_RESOURCES, ".png", "textures")) {
                return candidate;
            }
        }
        return null;
    }

    private static void saveStable(CachedOutput cache, List<CompletableFuture<?>> futures,
                                   JsonObject json, Path path) {
        futures.add(DataProvider.saveStable(cache, json, path));
    }

    @Override
    public String getName() {
        return ExtForCore.MOD_NAME + " material icon models";
    }
}
