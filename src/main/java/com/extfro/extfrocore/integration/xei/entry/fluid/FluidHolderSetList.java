package com.extfro.extfrocore.integration.xei.entry.fluid;

import net.minecraft.core.HolderSet;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class FluidHolderSetList implements FluidEntryList {

    @Getter
    private final List<FluidHolderSetEntry> entries = new ArrayList<>();

    public static FluidHolderSetList of(@NotNull HolderSet<Fluid> fluids, int amount) {
        FluidHolderSetList list = new FluidHolderSetList();
        list.add(fluids, amount);
        return list;
    }

    public void add(FluidHolderSetEntry entry) {
        entries.add(entry);
    }

    public void add(@NotNull HolderSet<Fluid> fluids, int amount) {
        add(new FluidHolderSetEntry(fluids, amount));
    }

    @Override
    public List<FluidStack> getStacks() {
        return entries.stream().flatMap(FluidHolderSetEntry::stacks).toList();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public record FluidHolderSetEntry(@NotNull HolderSet<Fluid> fluids, int amount) {

        public Stream<FluidStack> stacks() {
            return fluids.stream().map(holder -> new FluidStack(holder, amount));
        }
    }
}
