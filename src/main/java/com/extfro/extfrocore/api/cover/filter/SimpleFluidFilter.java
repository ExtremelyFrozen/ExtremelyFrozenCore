package com.extfro.extfrocore.api.cover.filter;

import com.extfro.extfrocore.api.sync_system.SyncedComponents;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SimpleFluidFilter implements FluidFilter {

    public static final Codec<SimpleFluidFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("is_blacklist").forGetter(SimpleFluidFilter::isBlackList),
            Codec.BOOL.fieldOf("ignore_components").forGetter(SimpleFluidFilter::isIgnoreComponents),
            FluidStack.OPTIONAL_CODEC.listOf().fieldOf("matches")
                    .forGetter(filter -> Arrays.stream(filter.matches).toList()))
            .apply(instance, SimpleFluidFilter::new));

    @Getter
    protected boolean isBlackList;
    @Getter
    protected boolean ignoreComponents;
    @Getter
    protected FluidStack[] matches = new FluidStack[9];
    @Getter
    protected int maxStackSize = 1;

    protected Consumer<SimpleFluidFilter> itemWriter = filter -> {};
    protected Consumer<SimpleFluidFilter> onUpdated = filter -> itemWriter.accept(filter);

    public SimpleFluidFilter() {
        Arrays.fill(matches, FluidStack.EMPTY);
    }

    public SimpleFluidFilter(boolean isBlackList, boolean ignoreComponents, List<FluidStack> matches) {
        this();
        this.isBlackList = isBlackList;
        this.ignoreComponents = ignoreComponents;
        for (int i = 0; i < Math.min(this.matches.length, matches.size()); i++) {
            this.matches[i] = matches.get(i);
        }
    }

    public static SimpleFluidFilter loadFilter(ItemStack itemStack) {
        SimpleFluidFilter filter = itemStack.getOrDefault(SyncedComponents.SIMPLE_FLUID_FILTER.get(),
                new SimpleFluidFilter());
        filter.itemWriter = updated -> itemStack.set(SyncedComponents.SIMPLE_FLUID_FILTER.get(), updated);
        return filter;
    }

    @Override
    public void setOnUpdated(Consumer<FluidFilter> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }

    @Override
    public boolean isBlank() {
        return !isBlackList && !ignoreComponents && Arrays.stream(matches).allMatch(FluidStack::isEmpty);
    }

    public void setBlackList(boolean blackList) {
        isBlackList = blackList;
        onUpdated.accept(this);
    }

    public void setIgnoreComponents(boolean ignoreComponents) {
        this.ignoreComponents = ignoreComponents;
        onUpdated.accept(this);
    }

    public void setMatch(int slot, FluidStack fluidStack) {
        if (slot < 0 || slot >= matches.length) {
            throw new IndexOutOfBoundsException(slot);
        }
        FluidStack copy = fluidStack.copy();
        if (!copy.isEmpty()) {
            copy.setAmount(Math.min(copy.getAmount(), maxStackSize));
        }
        matches[slot] = copy;
        onUpdated.accept(this);
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
        for (FluidStack match : matches) {
            if (!match.isEmpty()) {
                match.setAmount(Math.min(match.getAmount(), maxStackSize));
            }
        }
        onUpdated.accept(this);
    }

    @Override
    public UIElement openConfigurator(int x, int y) {
        return new UIElement().layout(layout -> {
            layout.width(18 * 3 + 25);
            layout.height(18 * 3);
        });
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return testFluidAmount(fluidStack) > 0;
    }

    @Override
    public int testFluidAmount(FluidStack fluidStack) {
        int totalFluidAmount = getTotalConfiguredFluidAmount(fluidStack);
        if (isBlackList) {
            return totalFluidAmount > 0 ? 0 : Integer.MAX_VALUE;
        }
        return totalFluidAmount;
    }

    public int getTotalConfiguredFluidAmount(FluidStack fluidStack) {
        int totalAmount = 0;
        for (FluidStack candidate : matches) {
            if (ignoreComponents) {
                if (FluidStack.isSameFluid(candidate, fluidStack)) {
                    totalAmount += candidate.getAmount();
                }
            } else if (FluidStack.isSameFluidSameComponents(candidate, fluidStack)) {
                totalAmount += candidate.getAmount();
            }
        }
        return totalAmount;
    }
}
