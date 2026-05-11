package com.extfro.extfrocore.api.sync_system;

import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SyncTagMap {

    public static final Codec<SyncTagMap> CODEC = Codec.PASSTHROUGH.comapFlatMap(
            dynamic -> decodeMap(dynamic.convert(NbtOps.INSTANCE)),
            map -> new Dynamic<>(NbtOps.INSTANCE, map.toTag()));

    private final Map<String, Tag> values;

    public SyncTagMap() {
        this(new LinkedHashMap<>());
    }

    private SyncTagMap(Map<String, Tag> values) {
        this.values = values;
    }

    public static SyncTagMap empty() {
        return new SyncTagMap();
    }

    private static DataResult<SyncTagMap> decodeMap(Dynamic<Tag> dynamic) {
        return dynamic.getMapValues().map(values -> {
            SyncTagMap map = empty();
            values.forEach((keyDynamic, valueDynamic) -> {
                String key = keyDynamic.asString().result().orElse(null);
                if (key != null) {
                    map.put(key, valueDynamic.getValue());
                }
            });
            return map;
        });
    }

    public static @Nullable SyncTagMap tryRead(Tag tag) {
        if (tag == EndTag.INSTANCE) {
            return empty();
        }
        return decodeMap(new Dynamic<>(NbtOps.INSTANCE, tag)).result().orElse(null);
    }

    public static boolean isEmptyContainer(Tag tag) {
        return tag == EndTag.INSTANCE;
    }

    public static Tag emptyContainer() {
        return EndTag.INSTANCE;
    }

    public Map<String, Tag> values() {
        return values;
    }

    public Tag toTag() {
        return NbtOps.INSTANCE.createMap(values.entrySet().stream()
                .map(entry -> Pair.of(NbtOps.INSTANCE.createString(entry.getKey()), entry.getValue())));
    }

    public void put(String key, Tag value) {
        values.put(key, value);
    }

    public Tag get(String key) {
        return values.get(key);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public SyncTagMap merge(SyncTagMap other) {
        values.putAll(other.values);
        return this;
    }
}
