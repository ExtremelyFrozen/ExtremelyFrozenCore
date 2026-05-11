package com.extfro.extfrocore.integration.xei.oreprocessing;

import com.extfro.extfrocore.integration.xei.entry.fluid.FluidEntryList;
import com.extfro.extfrocore.integration.xei.entry.item.ItemEntryList;
import com.extfro.extfrocore.integration.xei.entry.item.ItemStackList;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record OreProcessingDisplay(ResourceLocation id, OreProcessingCategory category, Component title,
                                   ItemEntryList primaryInput, List<OreProcessingStep> steps) {

    public OreProcessingDisplay {
        steps = List.copyOf(steps);
    }

    public static Builder builder(ResourceLocation id, OreProcessingCategory category, Component title,
                                  ItemEntryList primaryInput) {
        return new Builder(id, category, title, primaryInput);
    }

    public List<ItemEntryList> itemInputs() {
        List<ItemEntryList> inputs = new ArrayList<>();
        inputs.add(primaryInput);
        for (OreProcessingStep step : steps) {
            inputs.addAll(step.itemInputs());
        }
        return List.copyOf(inputs);
    }

    public List<FluidEntryList> fluidInputs() {
        List<FluidEntryList> inputs = new ArrayList<>();
        for (OreProcessingStep step : steps) {
            inputs.addAll(step.fluidInputs());
        }
        return List.copyOf(inputs);
    }

    public List<ItemStack> itemOutputs() {
        List<ItemStack> outputs = new ArrayList<>();
        for (OreProcessingStep step : steps) {
            for (ItemStack output : step.itemOutputs()) {
                outputs.add(output.copy());
            }
        }
        return List.copyOf(outputs);
    }

    public static final class Builder {

        private final ResourceLocation id;
        private final OreProcessingCategory category;
        private final Component title;
        private final ItemEntryList primaryInput;
        private final List<OreProcessingStep> steps = new ArrayList<>();

        private Builder(ResourceLocation id, OreProcessingCategory category, Component title,
                        ItemEntryList primaryInput) {
            this.id = id;
            this.category = category;
            this.title = title;
            this.primaryInput = primaryInput;
        }

        public static Builder ofStack(ResourceLocation id, OreProcessingCategory category, Component title,
                                      ItemStack primaryInput) {
            return new Builder(id, category, title, ItemStackList.of(primaryInput.copy()));
        }

        public Builder step(OreProcessingStep step) {
            this.steps.add(step);
            return this;
        }

        public Builder steps(Collection<OreProcessingStep> steps) {
            this.steps.addAll(steps);
            return this;
        }

        public OreProcessingDisplay build() {
            return new OreProcessingDisplay(id, category, title, primaryInput, steps);
        }
    }
}
