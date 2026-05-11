package com.extfro.extfrocore.api.recipe.content;

import net.minecraft.network.RegistryFriendlyByteBuf;

import com.mojang.serialization.Codec;

public final class SerializerLong implements IContentSerializer<Long> {

    public static final SerializerLong INSTANCE = new SerializerLong();

    private SerializerLong() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, Long content) {
        buf.writeVarLong(content);
    }

    @Override
    public Long fromNetwork(RegistryFriendlyByteBuf buf) {
        return buf.readVarLong();
    }

    @Override
    public Long of(Object object) {
        if (object instanceof Long value) {
            return value;
        }
        if (object instanceof Number value) {
            return value.longValue();
        }
        if (object instanceof CharSequence value) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue();
    }

    @Override
    public Long defaultValue() {
        return 0L;
    }

    @Override
    public Class<Long> contentClass() {
        return Long.class;
    }

    @Override
    public Codec<Long> codec() {
        return Codec.LONG;
    }
}
