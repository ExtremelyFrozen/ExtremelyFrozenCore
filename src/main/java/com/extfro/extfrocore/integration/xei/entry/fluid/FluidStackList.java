package com.extfro.extfrocore.integration.xei.entry.fluid;

import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public final class FluidStackList implements FluidEntryList {

    private final List<FluidStack> stacks;

    public FluidStackList() {
        this.stacks = new ArrayList<>();
    }

    public FluidStackList(List<FluidStack> stacks) {
        this.stacks = new ArrayList<>(stacks);
    }

    public static FluidStackList of(FluidStack stack) {
        FluidStackList list = new FluidStackList();
        list.add(stack);
        return list;
    }

    public static FluidStackList of(Collection<FluidStack> stacks) {
        FluidStackList list = new FluidStackList();
        list.addAll(stacks);
        return list;
    }

    public void add(FluidStack stack) {
        stacks.add(stack);
    }

    public void addAll(Collection<FluidStack> stacks) {
        this.stacks.addAll(stacks);
    }

    @Override
    public List<FluidStack> getStacks() {
        return stacks;
    }

    @Override
    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    public Stream<FluidStack> stream() {
        return stacks.stream();
    }
}
