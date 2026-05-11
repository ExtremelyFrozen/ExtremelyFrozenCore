package com.extfro.extfrocore.integration.xei.entry.fluid;

import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public sealed interface FluidEntryList permits FluidHolderSetList, FluidStackList, FluidTagList {

    List<FluidStack> getStacks();

    boolean isEmpty();
}
