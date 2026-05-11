package com.extfro.extfrocore.integration.xei.oreprocessing;

import com.extfro.extfrocore.integration.xei.entry.fluid.FluidEntryList;
import com.extfro.extfrocore.integration.xei.entry.fluid.FluidStackList;
import com.extfro.extfrocore.integration.xei.entry.item.ItemEntryList;
import com.extfro.extfrocore.integration.xei.entry.item.ItemStackList;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record OreProcessingStep(ResourceLocation id, Component title, List<ItemEntryList> itemInputs,
                                List<FluidEntryList> fluidInputs, List<ItemStack> itemOutputs,
                                List<FluidStack> fluidOutputs, List<OreProcessingChance> itemOutputChances,
                                List<OreProcessingChance> fluidOutputChances, boolean optional) {

    public OreProcessingStep {
        itemInputs = List.copyOf(itemInputs);
        fluidInputs = List.copyOf(fluidInputs);
        itemOutputs = copyItemStacks(itemOutputs);
        fluidOutputs = copyFluidStacks(fluidOutputs);
        itemOutputChances = copyChances(itemOutputChances, itemOutputs.size(), "itemOutputChances");
        fluidOutputChances = copyChances(fluidOutputChances, fluidOutputs.size(), "fluidOutputChances");
    }

    public static Builder builder(ResourceLocation id, Component title) {
        return new Builder(id, title);
    }

    private static List<ItemStack> copyItemStacks(Collection<ItemStack> stacks) {
        List<ItemStack> copy = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            copy.add(stack.copy());
        }
        return List.copyOf(copy);
    }

    private static List<FluidStack> copyFluidStacks(Collection<FluidStack> stacks) {
        List<FluidStack> copy = new ArrayList<>(stacks.size());
        for (FluidStack stack : stacks) {
            copy.add(stack.copy());
        }
        return List.copyOf(copy);
    }

    private static List<OreProcessingChance> copyChances(List<OreProcessingChance> chances, int expectedSize,
                                                         String name) {
        if (chances.size() > expectedSize) {
            throw new IllegalArgumentException(name + " cannot be larger than its output list");
        }
        List<OreProcessingChance> copy = new ArrayList<>(expectedSize);
        copy.addAll(chances);
        while (copy.size() < expectedSize) {
            copy.add(OreProcessingChance.GUARANTEED);
        }
        return List.copyOf(copy);
    }

    public static final class Builder {

        private final ResourceLocation id;
        private final Component title;
        private final List<ItemEntryList> itemInputs = new ArrayList<>();
        private final List<FluidEntryList> fluidInputs = new ArrayList<>();
        private final List<ItemStack> itemOutputs = new ArrayList<>();
        private final List<FluidStack> fluidOutputs = new ArrayList<>();
        private final List<OreProcessingChance> itemOutputChances = new ArrayList<>();
        private final List<OreProcessingChance> fluidOutputChances = new ArrayList<>();
        private boolean optional;

        private Builder(ResourceLocation id, Component title) {
            this.id = id;
            this.title = title;
        }

        public Builder itemInput(ItemStack stack) {
            return itemInput(ItemStackList.of(stack.copy()));
        }

        public Builder itemInput(ItemEntryList input) {
            this.itemInputs.add(input);
            return this;
        }

        public Builder itemInputs(Collection<ItemEntryList> inputs) {
            this.itemInputs.addAll(inputs);
            return this;
        }

        public Builder fluidInput(FluidStack stack) {
            return fluidInput(FluidStackList.of(stack.copy()));
        }

        public Builder fluidInput(FluidEntryList input) {
            this.fluidInputs.add(input);
            return this;
        }

        public Builder fluidInputs(Collection<FluidEntryList> inputs) {
            this.fluidInputs.addAll(inputs);
            return this;
        }

        public Builder itemOutput(ItemStack stack) {
            return itemOutput(stack, null);
        }

        public Builder itemOutput(ItemStack stack, @Nullable OreProcessingChance chance) {
            this.itemOutputs.add(stack.copy());
            this.itemOutputChances.add(chance == null ? OreProcessingChance.GUARANTEED : chance);
            return this;
        }

        public Builder fluidOutput(FluidStack stack) {
            return fluidOutput(stack, null);
        }

        public Builder fluidOutput(FluidStack stack, @Nullable OreProcessingChance chance) {
            this.fluidOutputs.add(stack.copy());
            this.fluidOutputChances.add(chance == null ? OreProcessingChance.GUARANTEED : chance);
            return this;
        }

        public Builder optional() {
            this.optional = true;
            return this;
        }

        public OreProcessingStep build() {
            return new OreProcessingStep(id, title, itemInputs, fluidInputs, itemOutputs, fluidOutputs,
                    itemOutputChances, fluidOutputChances, optional);
        }
    }
}
