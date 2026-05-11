package com.extfro.extfrocore.client.model.machine;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MachineRenderState;
import com.extfro.extfrocore.api.registry.EFRegistries;
import com.extfro.extfrocore.client.model.BasicUnbakedModel;
import com.extfro.extfrocore.client.model.machine.multipart.MultiPartSelector;
import com.extfro.extfrocore.client.model.machine.multipart.MultiPartUnbakedModel;
import com.extfro.extfrocore.client.model.machine.variant.MultiVariantModel;
import com.extfro.extfrocore.client.model.machine.variant.VariantState;
import com.extfro.extfrocore.client.renderer.machine.DynamicMachineRender;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.ExtendedBlockModelDeserializer;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.common.util.TransformationHelper;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import com.mojang.serialization.JsonOps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class MachineModelLoader implements IGeometryLoader<UnbakedMachineModel> {

    public static final MachineModelLoader INSTANCE = new MachineModelLoader();
    public static final ResourceLocation ID = ExtForCore.id("machine");
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BlockModel.class, new ExtendedBlockModelDeserializer())
            .registerTypeAdapter(BlockElement.class, new BlockElement.Deserializer())
            .registerTypeAdapter(BlockElementFace.class, new BlockElementFace.Deserializer())
            .registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer())
            .registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
            .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
            .registerTypeAdapter(ItemOverride.class, new ItemOverride.Deserializer())
            .registerTypeAdapter(Transformation.class, new TransformationHelper.Deserializer())
            .registerTypeAdapter(MultiVariantModel.class, new MultiVariantModel.Deserializer())
            .registerTypeAdapter(VariantState.class, new VariantState.Deserializer())
            .registerTypeAdapter(MultiPartSelector.class, new MultiPartSelector.Deserializer())
            .create();
    private static final Logger LOGGER = LogManager.getLogger("EF MACHINE MODEL LOADER");
    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);
    public static final UnbakedModel MISSING_MARKER = new BasicUnbakedModel();

    private MachineModelLoader() {
    }

    @Override
    public @Nullable UnbakedMachineModel read(JsonObject json, JsonDeserializationContext context)
            throws JsonParseException {
        ResourceLocation machineId = ResourceLocation.parse(GsonHelper.getAsString(json, "machine"));
        MachineDefinition definition = EFRegistries.MACHINES.get(machineId);
        if (definition == null) {
            return null;
        }

        Map<String, UnbakedModel> variants = new HashMap<>();
        if (json.has("variants")) {
            JsonObject variantsJson = GsonHelper.getAsJsonObject(json, "variants");
            for (Map.Entry<String, JsonElement> entry : variantsJson.entrySet()) {
                variants.put(entry.getKey(), GSON.fromJson(entry.getValue(), MultiVariantModel.class));
            }
        }
        MultiPartUnbakedModel multiPart = null;
        if (json.has("multipart")) {
            JsonArray multipartJson = GsonHelper.getAsJsonArray(json, "multipart");
            multiPart = MultiPartUnbakedModel.deserialize(definition, multipartJson);
        }
        if (variants.isEmpty() && (multiPart == null || multiPart.getModels().isEmpty())) {
            throw new JsonParseException("Model for machine %s doesn't have 'variants' or 'multipart' defined"
                    .formatted(machineId));
        }

        List<DynamicMachineRender<?, ?>> dynamicRenders = new ArrayList<>();
        JsonArray dynamicRendersJson = GsonHelper.getAsJsonArray(json, "dynamic_renders", null);
        if (dynamicRendersJson != null) {
            for (JsonElement entry : dynamicRendersJson) {
                dynamicRenders.add(DynamicMachineRender.CODEC.parse(JsonOps.INSTANCE, entry).getOrThrow());
            }
        }

        StateDefinition<MachineDefinition, MachineRenderState> stateDefinition = definition.getStateDefinition();
        ImmutableList<MachineRenderState> possibleStates = stateDefinition.getPossibleStates();
        Map<MachineRenderState, UnbakedModel> statesToModels = new IdentityHashMap<>();
        Map<ModelResourceLocation, MachineRenderState> modelsToStates = new HashMap<>();
        possibleStates.forEach(state -> modelsToStates.put(stateToModelLocation(machineId, state), state));
        MultiPartUnbakedModel finalMultiPart = multiPart;
        if (finalMultiPart != null) {
            possibleStates.forEach(state -> statesToModels.put(state, finalMultiPart));
        }

        try {
            variants.forEach((key, curModel) -> {
                try {
                    possibleStates.stream().filter(predicate(stateDefinition, key)).forEach(state -> {
                        UnbakedModel previous = statesToModels.put(state, curModel);
                        if (previous != null && previous != finalMultiPart) {
                            statesToModels.put(state, MISSING_MARKER);
                            throw new IllegalStateException("Overlapping definition for variant: " + key);
                        }
                    });
                } catch (Exception e) {
                    LOGGER.warn("Exception loading model for machine: '{}' for variant: '{}': {}", machineId, key, e);
                }
            });
        } finally {
            modelsToStates.forEach((modelLoc, state) -> {
                UnbakedModel unbaked = statesToModels.get(state);
                if (unbaked == null) {
                    LOGGER.warn("Exception loading model for machine: '{}' missing model for variant: '{}'",
                            machineId, modelLoc);
                    statesToModels.put(state, MISSING_MARKER);
                }
            });
        }

        ResourceLocation particle = json.has("particle") ?
                ResourceLocation.parse(GsonHelper.getAsString(json, "particle")) : null;
        return new UnbakedMachineModel(definition, statesToModels, multiPart, dynamicRenders, particle);
    }

    protected static void resolveStateModels(UnbakedMachineModel model,
                                             Function<ResourceLocation, UnbakedModel> resolver) {
        UnbakedModel missingModel = resolver.apply(ModelBakery.MISSING_MODEL_LOCATION);
        MultiPartUnbakedModel multiPart = model.getMultiPart();
        if (multiPart != null) {
            multiPart.resolveParents(resolver);
        }
        Map<MachineRenderState, UnbakedModel> modelsCopy = new IdentityHashMap<>(model.getModels());
        modelsCopy.forEach((state, variant) -> {
            if (variant == null || variant == MISSING_MARKER) {
                model.getModels().put(state, missingModel);
            } else {
                variant.resolveParents(resolver);
                model.getModels().put(state, variant);
            }
        });
    }

    private static Predicate<MachineRenderState> predicate(StateDefinition<MachineDefinition, MachineRenderState> owner,
                                                           String variant) {
        Map<Property<?>, Comparable<?>> properties = Maps.newHashMap();
        for (String propertyEntry : COMMA_SPLITTER.split(variant)) {
            Iterator<String> keyValue = EQUAL_SPLITTER.split(propertyEntry).iterator();
            if (keyValue.hasNext()) {
                String key = keyValue.next();
                Property<?> property = owner.getProperty(key);
                if (property != null && keyValue.hasNext()) {
                    String value = keyValue.next();
                    Comparable<?> comparable = getValueHelper(property, value);
                    if (comparable == null) {
                        throw new RuntimeException("Unknown value: '" + value +
                                "' for machine model state property: '" + key + "' " + property.getPossibleValues());
                    }
                    properties.put(property, comparable);
                } else if (!key.isEmpty()) {
                    throw new RuntimeException("Unknown machine model state property: '" + key + "'");
                }
            }
        }

        MachineDefinition machine = owner.getOwner();
        return state -> {
            if (state == null || !state.is(machine)) {
                return false;
            }
            for (var entry : properties.entrySet()) {
                if (!Objects.equals(state.getValue(entry.getKey()), entry.getValue())) {
                    return false;
                }
            }
            return true;
        };
    }

    @Nullable
    static <T extends Comparable<T>> T getValueHelper(Property<T> property, String value) {
        return property.getValue(value).orElse(null);
    }

    public static ModelResourceLocation stateToModelLocation(ResourceLocation location, MachineRenderState state) {
        return new ModelResourceLocation(location, BlockModelShaper.statePropertiesToString(state.getValues()));
    }

    public static Either<ResourceLocation, UnbakedModel> parseVariant(JsonElement value,
                                                                      JsonDeserializationContext context) {
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            return Either.left(ResourceLocation.parse(value.getAsString()));
        }
        return Either.right(context.deserialize(value, BlockModel.class));
    }
}
