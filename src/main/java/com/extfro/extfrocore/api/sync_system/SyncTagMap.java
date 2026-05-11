package com.extfro.extfrocore.api.sync_system;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map-shaped sync data used by the sync system.
 * <p>
 * This keeps Minecraft's compound NBT type at the serialization boundary instead of exposing it through holder APIs.
 */
public final class SyncTagMap {

    public static final Codec<SyncTagMap> CODEC = CompoundTag.CODEC.xmap(SyncTagMap::fromTag, SyncTagMap::toTag);

    private final Map<String, Tag> values;

    public SyncTagMap() {
        this.values = new LinkedHashMap<>();
    }

    private SyncTagMap(Map<String, Tag> values) {
        this.values = values;
    }

    public static SyncTagMap empty() {
        return new SyncTagMap();
    }

    public static SyncTagMap fromTag(CompoundTag tag) {
        Map<String, Tag> values = new LinkedHashMap<>();
        for (String key : tag.getAllKeys()) {
            Tag value = tag.get(key);
            if (value != null) {
                values.put(key, value);
            }
        }
        return new SyncTagMap(values);
    }

    public static @Nullable SyncTagMap tryRead(Tag tag) {
        return tag instanceof CompoundTag compoundTag ? fromTag(compoundTag) : null;
    }

    public static boolean isEmptyContainer(Tag tag) {
        return tag instanceof CompoundTag compoundTag && compoundTag.isEmpty();
    }

    public static Tag emptyContainer() {
        return empty().toTag();
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        values.forEach(tag::put);
        return tag;
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
