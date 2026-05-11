package com.extfro.extfrocore.api.recipe.content;

import net.minecraft.network.RegistryFriendlyByteBuf;

import com.mojang.serialization.Codec;

public final class SerializerDouble implements IContentSerializer<Double> {

    public static final SerializerDouble INSTANCE = new SerializerDouble();

    private SerializerDouble() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, Double content) {
        buf.writeDouble(content);
    }

    @Override
    public Double fromNetwork(RegistryFriendlyByteBuf buf) {
        return buf.readDouble();
    }

    @Override
    public Double of(Object object) {
        if (object instanceof Double value) {
            return value;
        }
        if (object instanceof Number value) {
            return value.doubleValue();
        }
        if (object instanceof CharSequence value) {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue();
    }

    @Override
    public Double defaultValue() {
        return 0D;
    }

    @Override
    public Class<Double> contentClass() {
        return Double.class;
    }

    @Override
    public Codec<Double> codec() {
        return Codec.DOUBLE;
    }
}
