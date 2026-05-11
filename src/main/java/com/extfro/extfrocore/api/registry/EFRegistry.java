package com.extfro.extfrocore.api.registry;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class EFRegistry<K, V> implements Iterable<V> {

    public static final Map<ResourceLocation, EFRegistry<?, ?>> REGISTERED = new HashMap<>();

    protected final Map<K, V> keyToValue;
    protected final Map<V, K> valueToKey;
    @Getter
    protected final ResourceLocation registryName;
    @Getter
    protected boolean frozen = true;

    public EFRegistry(ResourceLocation registryName) {
        this.keyToValue = new HashMap<>();
        this.valueToKey = new HashMap<>();
        this.registryName = registryName;

        REGISTERED.put(registryName, this);
    }

    public boolean containKey(K key) {
        return keyToValue.containsKey(key);
    }

    public boolean containValue(V value) {
        return keyToValue.containsValue(value);
    }

    public void freeze() {
        if (frozen) {
            throw new IllegalStateException("Registry is already frozen");
        }
        this.frozen = true;
    }

    public void unfreeze() {
        if (!frozen) {
            throw new IllegalStateException("Registry is already unfrozen");
        }
        this.frozen = false;
    }

    public <T extends V> T register(K key, T value) {
        if (keyToValue.containsKey(key)) {
            throw new IllegalStateException("[register] registry %s contains key %s already"
                    .formatted(registryName, key));
        }
        return registerOrOverride(key, value);
    }

    public void remap(K oldKey, K newKey) {
        if (frozen) {
            throw new IllegalStateException("[remap] registry %s has been frozen".formatted(registryName));
        }

        if (keyToValue.containsKey(oldKey)) {
            ExtForCore.LOGGER.warn("[remap] cannot remap existing key {} in registry {}", oldKey, registryName);
            return;
        }
        if (!keyToValue.containsKey(newKey)) {
            ExtForCore.LOGGER.warn("[remap] could not find value for key {} in registry {}", newKey, registryName);
            return;
        }
        V newValue = keyToValue.get(newKey);
        keyToValue.put(oldKey, newValue);
    }

    @Nullable
    public <T extends V> T replace(K key, T value) {
        if (!containKey(key)) {
            ExtForCore.LOGGER.warn("[replace] could not find key {} in registry {}", key, registryName);
        }

        return registerOrOverride(key, value);
    }

    public <T extends V> T registerOrOverride(K key, T value) {
        if (frozen) {
            throw new IllegalStateException("[register] registry %s has been frozen".formatted(registryName));
        }
        keyToValue.put(key, value);
        valueToKey.put(value, key);

        return value;
    }

    @NotNull
    @Override
    public @UnmodifiableView Iterator<V> iterator() {
        return registry().values().iterator();
    }

    public @UnmodifiableView Set<V> values() {
        return Collections.unmodifiableMap(valueToKey).keySet();
    }

    public @UnmodifiableView Set<K> keys() {
        return registry().keySet();
    }

    public @UnmodifiableView Set<Map.Entry<K, V>> entries() {
        return registry().entrySet();
    }

    public @UnmodifiableView Map<K, V> registry() {
        return Collections.unmodifiableMap(keyToValue);
    }

    public void clear() {
        if (frozen) {
            throw new IllegalArgumentException("Registry is frozen");
        }
        keyToValue.clear();
        valueToKey.clear();
    }

    @Nullable
    public V get(K key) {
        return keyToValue.get(key);
    }

    public V getOrDefault(K key, V defaultValue) {
        return keyToValue.getOrDefault(key, defaultValue);
    }

    public K getKey(V value) {
        return valueToKey.get(value);
    }

    public K getOrDefaultKey(V value, K defaultKey) {
        return valueToKey.getOrDefault(value, defaultKey);
    }

    public boolean remove(K name) {
        var value = keyToValue.remove(name);
        if (value != null) {
            valueToKey.remove(value);
            return true;
        }
        return false;
    }

    public abstract void writeBuf(V value, FriendlyByteBuf buf);

    @Nullable
    public abstract V readBuf(FriendlyByteBuf buf);

    public abstract Tag saveToTag(V value);

    @Nullable
    public abstract V loadFromTag(Tag tag);

    public abstract Codec<V> codec();

    public static class StringKey<V> extends EFRegistry<String, V> {

        public StringKey(ResourceLocation registryName) {
            super(registryName);
        }

        @Override
        public void writeBuf(V value, FriendlyByteBuf buf) {
            buf.writeBoolean(containValue(value));
            if (containValue(value)) {
                buf.writeUtf(getKey(value));
            }
        }

        @Override
        public V readBuf(FriendlyByteBuf buf) {
            return buf.readBoolean() ? get(buf.readUtf()) : null;
        }

        @Override
        public Tag saveToTag(V value) {
            return containValue(value) ? StringTag.valueOf(getKey(value)) : EndTag.INSTANCE;
        }

        @Override
        public V loadFromTag(Tag tag) {
            return tag == EndTag.INSTANCE ? null : get(tag.getAsString());
        }

        @Override
        public Codec<V> codec() {
            return Codec.STRING.flatXmap(
                    key -> Optional.ofNullable(this.get(key))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(
                                    () -> "Unknown registry key in %s: %s".formatted(this.registryName, key))),
                    val -> Optional.ofNullable(this.getKey(val))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(
                                    () -> "Unknown registry value in %s: %s".formatted(this.registryName, val))));
        }
    }

    public static class ResourceKey<V> extends EFRegistry<ResourceLocation, V> {

        public ResourceKey(ResourceLocation registryName) {
            super(registryName);
        }

        @Override
        public void writeBuf(V value, FriendlyByteBuf buf) {
            buf.writeBoolean(containValue(value));
            if (containValue(value)) {
                buf.writeResourceLocation(getKey(value));
            }
        }

        @Override
        public V readBuf(FriendlyByteBuf buf) {
            return buf.readBoolean() ? get(buf.readResourceLocation()) : null;
        }

        @Override
        public Tag saveToTag(V value) {
            return containValue(value) ? StringTag.valueOf(getKey(value).toString()) : EndTag.INSTANCE;
        }

        @Override
        public V loadFromTag(Tag tag) {
            return tag == EndTag.INSTANCE ? null : get(ResourceLocation.parse(tag.getAsString()));
        }

        @Override
        public Codec<V> codec() {
            return ResourceLocation.CODEC.flatXmap(
                    key -> Optional.ofNullable(this.get(key))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(
                                    () -> "Unknown registry key in %s: %s".formatted(this.registryName, key))),
                    val -> Optional.ofNullable(this.getKey(val))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(
                                    () -> "Unknown registry value in %s: %s".formatted(this.registryName, val))));
        }
    }
}
