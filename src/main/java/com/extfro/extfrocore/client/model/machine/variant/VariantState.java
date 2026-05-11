package com.extfro.extfrocore.client.model.machine.variant;

import com.extfro.extfrocore.client.model.machine.MachineModelLoader;
import com.extfro.extfrocore.client.util.VariantRotationHelpers;

import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.Objects;

public class VariantState implements ModelState {

    @Getter
    private final Either<ResourceLocation, UnbakedModel> model;
    @Getter
    private final Transformation rotation;
    @Getter
    private final boolean uvLocked;
    @Getter
    private final int weight;
    @Getter
    @Setter
    private UnbakedModel resolvedModel;

    public VariantState(Either<ResourceLocation, UnbakedModel> model, Transformation rotation,
                        boolean uvLocked, int weight) {
        this.model = model;
        this.rotation = rotation;
        this.uvLocked = uvLocked;
        this.weight = weight;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof VariantState variantState)) return false;
        return uvLocked == variantState.uvLocked && weight == variantState.weight &&
                model.equals(variantState.model) && Objects.equals(rotation, variantState.rotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, rotation, uvLocked, weight);
    }

    public static class Deserializer implements JsonDeserializer<VariantState> {

        @Override
        public VariantState deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            var model = MachineModelLoader.parseVariant(object.get("model"), context);
            Transformation rotation = getBlockRotation(object);
            boolean uvLock = GsonHelper.getAsBoolean(object, "uvlock", false);
            int weight = getWeight(object);
            return new VariantState(model, rotation, uvLock, weight);
        }

        protected Transformation getBlockRotation(JsonObject json) {
            int x = GsonHelper.getAsInt(json, "x", 0);
            int y = GsonHelper.getAsInt(json, "y", 0);
            int z = GsonHelper.getAsInt(json, "z", 0);
            Transformation rotation = VariantRotationHelpers.getRotationTransform(x, y, z);
            if (rotation != null) {
                return rotation;
            }
            throw new JsonParseException("Invalid ExtendedBlockModelRotation x: " + x + ", y: " + y + ", z: " + z);
        }

        protected int getWeight(JsonObject json) {
            int weight = GsonHelper.getAsInt(json, "weight", 1);
            if (weight < 1) {
                throw new JsonParseException("Invalid weight " + weight + " found, expected integer >= 1");
            }
            return weight;
        }
    }
}
