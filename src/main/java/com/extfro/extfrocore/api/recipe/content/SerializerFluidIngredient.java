package com.extfro.extfrocore.api.recipe.content;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.mojang.serialization.Codec;

public class SerializerFluidIngredient implements IContentSerializer<SizedFluidIngredient> {

    public static final SizedFluidIngredient EMPTY = new SizedFluidIngredient(FluidIngredient.empty(), 1);
    public static final SerializerFluidIngredient INSTANCE = new SerializerFluidIngredient();

    private SerializerFluidIngredient() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, SizedFluidIngredient content) {
        SizedFluidIngredient.STREAM_CODEC.encode(buf, content);
    }

    @Override
    public SizedFluidIngredient fromNetwork(RegistryFriendlyByteBuf buf) {
        return SizedFluidIngredient.STREAM_CODEC.decode(buf);
    }

    @Override
    public SizedFluidIngredient of(Object object) {
        if (object instanceof SizedFluidIngredient ingredient) {
            return new SizedFluidIngredient(ingredient.ingredient(), ingredient.amount());
        }
        if (object instanceof FluidStack stack) {
            return SizedFluidIngredient.of(stack.copy());
        }
        return EMPTY;
    }

    @Override
    public SizedFluidIngredient defaultValue() {
        return EMPTY;
    }

    @Override
    public Class<SizedFluidIngredient> contentClass() {
        return SizedFluidIngredient.class;
    }

    @Override
    public Codec<SizedFluidIngredient> codec() {
        return SizedFluidIngredient.NESTED_CODEC;
    }
}
