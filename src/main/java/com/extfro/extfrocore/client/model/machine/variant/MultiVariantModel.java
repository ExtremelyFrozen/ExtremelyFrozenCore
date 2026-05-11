package com.extfro.extfrocore.client.model.machine.variant;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.SimpleModelState;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MultiVariantModel(List<VariantState> variants) implements UnbakedModel {

    @Override
    public @NotNull Collection<ResourceLocation> getDependencies() {
        return variants.stream()
                .map(VariantState::getModel)
                .flatMap(either -> either.map(Stream::of, model -> model.getDependencies().stream()))
                .collect(Collectors.toSet());
    }

    @Override
    public void resolveParents(@NotNull Function<ResourceLocation, UnbakedModel> resolver) {
        variants.forEach(variant -> {
            UnbakedModel model = variant.getModel().map(resolver, Function.identity());
            variant.setResolvedModel(model);
            model.resolveParents(resolver);
        });
    }

    @Override
    public @Nullable BakedModel bake(@NotNull ModelBaker baker,
                                     @NotNull Function<Material, TextureAtlasSprite> spriteGetter,
                                     @NotNull ModelState state) {
        if (variants.isEmpty()) {
            return null;
        }
        WeightedBakedModel.Builder weightedBuilder = new WeightedBakedModel.Builder();
        for (VariantState variant : variants) {
            var actualRotation = state.getRotation().compose(variant.getRotation());
            var actualState = new SimpleModelState(actualRotation, variant.isUvLocked());
            BakedModel baked = variant.getResolvedModel().bake(baker, spriteGetter, actualState);
            weightedBuilder.add(baked, variant.getWeight());
        }
        return weightedBuilder.build();
    }

    public static class Deserializer implements JsonDeserializer<MultiVariantModel> {

        @Override
        public MultiVariantModel deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                                                                                                              throws JsonParseException {
            List<VariantState> variants = new ArrayList<>();
            if (json.isJsonArray()) {
                JsonArray array = json.getAsJsonArray();
                if (array.isEmpty()) {
                    throw new JsonParseException("Empty variant array");
                }
                for (JsonElement value : array) {
                    variants.add(context.deserialize(value, VariantState.class));
                }
            } else {
                variants.add(context.deserialize(json, VariantState.class));
            }
            return new MultiVariantModel(variants);
        }
    }
}
