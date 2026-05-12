package com.extfro.extfrocore.api.fluids.attribute;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.network.chat.Component;

public final class FluidAttributes {

    /**
     * Attribute for acidic fluids.
     */
    public static final FluidAttribute ACID = new FluidAttribute(ExtForCore.id("acid"),
            list -> list.accept(Component.translatable("gtceu.fluid.type_acid.tooltip")),
            list -> list.accept(Component.translatable("gtceu.fluid_pipe.acid_proof")));

    private FluidAttributes() {}
}
