package com.extfro.extfrocore.api.recipe;

import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RecipeDataMap {

    public static final Codec<RecipeDataMap> CODEC = Codec.PASSTHROUGH.comapFlatMap(
            dynamic -> decode(dynamic.convert(NbtOps.INSTANCE)),
            map -> new Dynamic<>(NbtOps.INSTANCE, map.toTag()));

    private final Map<String, Tag> values;

    public RecipeDataMap() {
        this(new LinkedHashMap<>());
    }

    private RecipeDataMap(Map<String, Tag> values) {
        this.values = values;
    }

    public static RecipeDataMap empty() {
        return new RecipeDataMap();
    }

    private static DataResult<RecipeDataMap> decode(Dynamic<Tag> dynamic) {
        return dynamic.getMapValues().map(values -> {
            RecipeDataMap map = empty();
            values.forEach((keyDynamic, valueDynamic) -> {
                String key = keyDynamic.asString().result().orElse(null);
                if (key != null) {
                    map.put(key, valueDynamic.getValue());
                }
            });
            return map;
        });
    }

    public static RecipeDataMap read(RegistryFriendlyByteBuf buf) {
        Tag tag = buf.readNbt();
        if (tag == null || tag == EndTag.INSTANCE) {
            return empty();
        }
        return CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeNbt(isEmpty() ? EndTag.INSTANCE : toTag());
    }

    public Map<String, Tag> values() {
        return values;
    }

    public Tag toTag() {
        return NbtOps.INSTANCE.createMap(values.entrySet().stream()
                .map(entry -> Pair.of(StringTag.valueOf(entry.getKey()), entry.getValue())));
    }

    public void put(String key, Tag value) {
        values.put(key, value);
    }

    public @Nullable Tag get(String key) {
        return values.get(key);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public RecipeDataMap copy() {
        return new RecipeDataMap(new LinkedHashMap<>(values));
    }
}
