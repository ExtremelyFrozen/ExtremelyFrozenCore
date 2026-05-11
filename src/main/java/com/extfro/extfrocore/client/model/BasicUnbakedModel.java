package com.extfro.extfrocore.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class BasicUnbakedModel implements UnbakedModel {

    @Override
    public @NotNull Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void resolveParents(@NotNull Function<ResourceLocation, UnbakedModel> resolver) {
        for (ResourceLocation dependency : getDependencies()) {
            resolver.apply(dependency).resolveParents(resolver);
        }
    }

    @Override
    public @Nullable BakedModel bake(@NotNull ModelBaker baker,
                                     @NotNull Function<Material, TextureAtlasSprite> spriteGetter,
                                     @NotNull ModelState state) {
        return null;
    }
}
