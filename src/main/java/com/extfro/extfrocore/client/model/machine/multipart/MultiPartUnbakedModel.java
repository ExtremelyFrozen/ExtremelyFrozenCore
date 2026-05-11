package com.extfro.extfrocore.client.model.machine.multipart;

import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MachineRenderState;
import com.extfro.extfrocore.client.model.machine.MachineModelLoader;
import com.extfro.extfrocore.client.model.machine.variant.MultiVariantModel;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.StateDefinition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public record MultiPartUnbakedModel(StateDefinition<MachineDefinition, MachineRenderState> definition,
                                    List<MultiPartSelector> selectors)
        implements UnbakedModel {

    public Set<MultiVariantModel> getModels() {
        Set<MultiVariantModel> set = new HashSet<>();
        for (MultiPartSelector selector : selectors()) {
            set.add(selector.getVariant());
        }
        return set;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return selectors().stream()
                .flatMap(selector -> selector.getVariant().getDependencies().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
        selectors().forEach(selector -> selector.getVariant().resolveParents(resolver));
    }

    @Override
    public MultiPartBakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter,
                                    ModelState state) {
        MultiPartBakedModel.Builder builder = new MultiPartBakedModel.Builder();
        for (MultiPartSelector selector : selectors()) {
            BakedModel bakedModel = selector.getVariant().bake(baker, spriteGetter, state);
            if (bakedModel != null) {
                builder.add(selector.getPredicate(definition), bakedModel);
            }
        }
        return builder.build();
    }

    public static MultiPartUnbakedModel deserialize(MachineDefinition definition, JsonArray elements) {
        return new MultiPartUnbakedModel(definition.getStateDefinition(), getSelectors(elements));
    }

    private static List<MultiPartSelector> getSelectors(JsonArray elements) {
        List<MultiPartSelector> list = new ArrayList<>();
        for (JsonElement element : elements) {
            list.add(MachineModelLoader.GSON.fromJson(element, MultiPartSelector.class));
        }
        return list;
    }
}
