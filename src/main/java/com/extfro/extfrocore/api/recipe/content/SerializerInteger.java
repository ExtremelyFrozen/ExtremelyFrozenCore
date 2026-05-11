package com.extfro.extfrocore.api.recipe.content;

import net.minecraft.network.RegistryFriendlyByteBuf;

import com.mojang.serialization.Codec;

public final class SerializerInteger implements IContentSerializer<Integer> {

    public static final SerializerInteger INSTANCE = new SerializerInteger();

    private SerializerInteger() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, Integer content) {
        buf.writeInt(content);
    }

    @Override
    public Integer fromNetwork(RegistryFriendlyByteBuf buf) {
        return buf.readInt();
    }

    @Override
    public Integer of(Object object) {
        if (object instanceof Integer value) {
            return value;
        }
        if (object instanceof Number value) {
            return value.intValue();
        }
        if (object instanceof CharSequence value) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue();
    }

    @Override
    public Integer defaultValue() {
        return 0;
    }

    @Override
    public Class<Integer> contentClass() {
        return Integer.class;
    }

    @Override
    public Codec<Integer> codec() {
        return Codec.INT;
    }
}
