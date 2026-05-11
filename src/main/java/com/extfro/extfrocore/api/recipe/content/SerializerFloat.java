package com.extfro.extfrocore.api.recipe.content;

import net.minecraft.network.RegistryFriendlyByteBuf;

import com.mojang.serialization.Codec;

public final class SerializerFloat implements IContentSerializer<Float> {

    public static final SerializerFloat INSTANCE = new SerializerFloat();

    private SerializerFloat() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, Float content) {
        buf.writeFloat(content);
    }

    @Override
    public Float fromNetwork(RegistryFriendlyByteBuf buf) {
        return buf.readFloat();
    }

    @Override
    public Float of(Object object) {
        if (object instanceof Float value) {
            return value;
        }
        if (object instanceof Number value) {
            return value.floatValue();
        }
        if (object instanceof CharSequence value) {
            try {
                return Float.parseFloat(value.toString());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue();
    }

    @Override
    public Float defaultValue() {
        return 0F;
    }

    @Override
    public Class<Float> contentClass() {
        return Float.class;
    }

    @Override
    public Codec<Float> codec() {
        return Codec.FLOAT;
    }
}
