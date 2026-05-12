package com.extfro.extfrocore.client.model.pipe;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.client.model.machine.MachineModelLoader;
import com.extfro.extfrocore.client.model.machine.variant.MultiVariantModel;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PipeModelLoader implements IGeometryLoader<UnbakedPipeModel> {

    public static final PipeModelLoader INSTANCE = new PipeModelLoader();
    public static final ResourceLocation ID = ExtForCore.id("pipe");
    public static final String PRIMARY_CENTER_KEY = "center";
    public static final Set<String> CENTER_KEYS = Set.of(PRIMARY_CENTER_KEY, "core", "null", "none");

    @Override
    public @Nullable UnbakedPipeModel read(JsonObject json,
                                           JsonDeserializationContext context) throws JsonParseException {
        // load the inner models
        final Map<Direction, UnbakedModel> parts = new HashMap<>();
        if (json.has("parts")) {
            JsonObject variantsJson = GsonHelper.getAsJsonObject(json, "parts");
            for (Map.Entry<String, JsonElement> entry : variantsJson.entrySet()) {
                Direction direction = Direction.byName(entry.getKey());
                if (direction == null && !CENTER_KEYS.contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                    throw new JsonParseException("Invalid pipe model part specifier " + entry.getKey());
                }
                if (direction == null && parts.get(null) != null) {
                    throw new JsonParseException("Cannot specify more than one 'center' model for a pipe model");
                }

                parts.put(direction, MachineModelLoader.GSON.fromJson(entry.getValue(), MultiVariantModel.class));
            }
        }
        // and the restrictors
        final Map<Direction, UnbakedModel> restrictors = new HashMap<>();
        if (json.has("restrictors")) {
            JsonObject variantsJson = GsonHelper.getAsJsonObject(json, "restrictors");
            for (Map.Entry<String, JsonElement> entry : variantsJson.entrySet()) {
                Direction direction = Direction.byName(entry.getKey());
                if (direction == null) {
                    throw new JsonParseException("Invalid pipe model part specifier " + entry.getKey());
                }
                restrictors.put(direction, MachineModelLoader.GSON.fromJson(entry.getValue(), MultiVariantModel.class));
            }
        }

        return new UnbakedPipeModel(parts, restrictors);
    }
}
