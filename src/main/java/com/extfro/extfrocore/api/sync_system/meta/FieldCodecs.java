package com.extfro.extfrocore.api.sync_system.meta;

import com.extfro.extfrocore.api.sync_system.SyncTagMap;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class FieldCodecs {

    private static final Map<Type, Codec<?>> REGISTRY = new Reference2ReferenceOpenHashMap<>();
    private static final Map<Type, Supplier<Codec<?>>> SUPPLIERS = new Reference2ReferenceOpenHashMap<>();

    private static final Map<Type, Type> PRIMITIVE_TO_BOXED = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            void.class, Void.class);

    public static @Nullable Codec<?> get(Type type) {
        if (type instanceof Class<?> cls && cls.isPrimitive()) {
            type = PRIMITIVE_TO_BOXED.get(cls);
        }

        Codec<?> cached = REGISTRY.get(type);
        if (cached != null) {
            return cached;
        }

        if (type instanceof ParameterizedType pt) {
            Class<?> raw = (Class<?>) pt.getRawType();
            if (List.class.isAssignableFrom(raw)) {
                return makeListCodec(pt);
            }
            if (Set.class.isAssignableFrom(raw)) {
                return makeSetCodec(pt);
            }
            if (Map.class.isAssignableFrom(raw)) {
                return makeMapCodec(pt);
            }

            for (var entry : SUPPLIERS.entrySet()) {
                if (entry.getKey() instanceof Class<?> supplierType && supplierType.isAssignableFrom(raw)) {
                    return entry.getValue().get();
                }
            }
        }

        var declaration = new TypeDeclaration(type);
        Class<?> clazz = declaration.getClassValue();
        if (clazz == null) {
            return null;
        }

        if (clazz.isArray()) {
            return makeArrayCodec(clazz.getComponentType());
        }

        if (clazz.isEnum()) {
            return makeEnumCodec(clazz);
        }

        for (var entry : SUPPLIERS.entrySet()) {
            if (entry.getKey() instanceof Class<?> supplierType && supplierType.isAssignableFrom(clazz)) {
                return entry.getValue().get();
            }
        }

        return null;
    }

    public static void register(Type type, Codec<?> codec) {
        REGISTRY.putIfAbsent(type, codec);
    }

    public static void registerSupplier(Class<?> type, Supplier<Codec<?>> supplier) {
        SUPPLIERS.putIfAbsent(type, supplier);
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable Codec<T> getTyped(Type type) {
        return (Codec<T>) get(type);
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Codec<?> makeListCodec(ParameterizedType pt) {
        Codec<?> elem = get(pt.getActualTypeArguments()[0]);
        if (elem == null) {
            return null;
        }
        return Codec.list((Codec<Object>) elem);
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Codec<?> makeSetCodec(ParameterizedType pt) {
        Codec<?> elem = get(pt.getActualTypeArguments()[0]);
        if (elem == null) {
            return null;
        }
        return Codec.list((Codec<Object>) elem).xmap(LinkedHashSet::new, ArrayList::new);
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Codec<?> makeMapCodec(ParameterizedType pt) {
        Codec<?> key = get(pt.getActualTypeArguments()[0]);
        Codec<?> val = get(pt.getActualTypeArguments()[1]);
        if (key == null || val == null) {
            return null;
        }
        return Codec.unboundedMap((Codec<Object>) key, (Codec<Object>) val);
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Codec<?> makeArrayCodec(Class<?> component) {
        Codec<?> elem = get(component);
        if (elem == null) {
            return null;
        }
        return Codec.list((Codec<Object>) elem).xmap(
                list -> {
                    var array = Array.newInstance(component, list.size());
                    for (int i = 0; i < list.size(); i++) {
                        Array.set(array, i, list.get(i));
                    }
                    return array;
                },
                array -> {
                    var list = new ArrayList<>();
                    for (int i = 0; i < Array.getLength(array); i++) {
                        list.add(Array.get(array, i));
                    }
                    return list;
                });
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Codec<?> makeEnumCodec(Class<?> clazz) {
        return Codec.STRING.xmap(
                name -> Enum.valueOf((Class) clazz, name),
                value -> ((Enum<?>) value).name());
    }

    static {
        register(Integer.class, Codec.INT);
        register(Long.class, Codec.LONG);
        register(Float.class, Codec.FLOAT);
        register(Double.class, Codec.DOUBLE);
        register(Short.class, Codec.SHORT);
        register(Byte.class, Codec.BYTE);
        register(Boolean.class, Codec.BOOL);
        register(Character.class, Codec.STRING.xmap(value -> value.charAt(0), String::valueOf));

        register(String.class, Codec.STRING);
        register(UUID.class, Codec.STRING.xmap(UUID::fromString, UUID::toString));
        register(int[].class, Codec.INT_STREAM.xmap(IntStream::toArray, Arrays::stream));
        register(long[].class, Codec.LONG_STREAM.xmap(LongStream::toArray, Arrays::stream));
        register(byte[].class, Codec.list(Codec.BYTE).xmap(
                list -> {
                    byte[] array = new byte[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        array[i] = list.get(i);
                    }
                    return array;
                },
                array -> {
                    var list = new ArrayList<Byte>();
                    for (byte value : array) {
                        list.add(value);
                    }
                    return list;
                }));

        register(BlockPos.class, BlockPos.CODEC);
        register(ResourceLocation.class, ResourceLocation.CODEC);
        register(ItemStack.class, ItemStack.CODEC);
        register(FluidStack.class, FluidStack.CODEC);
        register(Component.class, ComponentSerialization.CODEC);
        register(SyncTagMap.class, SyncTagMap.CODEC);
    }
}
