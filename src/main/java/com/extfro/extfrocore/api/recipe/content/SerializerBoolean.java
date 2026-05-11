package com.extfro.extfrocore.api.recipe.content;

import net.minecraft.network.RegistryFriendlyByteBuf;

import com.mojang.serialization.Codec;

public final class SerializerBoolean implements IContentSerializer<Boolean> {

    public static final SerializerBoolean INSTANCE = new SerializerBoolean();

    private SerializerBoolean() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, Boolean content) {
        buf.writeBoolean(content);
    }

    @Override
    public Boolean fromNetwork(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean();
    }

    @Override
    public Boolean of(Object object) {
        if (object instanceof Boolean value) {
            return value;
        }
        if (object instanceof CharSequence value) {
            return Boolean.parseBoolean(value.toString());
        }
        return defaultValue();
    }

    @Override
    public Boolean defaultValue() {
        return false;
    }

    @Override
    public Class<Boolean> contentClass() {
        return Boolean.class;
    }

    @Override
    public Codec<Boolean> codec() {
        return Codec.BOOL;
    }
}
