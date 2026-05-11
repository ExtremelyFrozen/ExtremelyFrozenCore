package com.extfro.extfrocore.api.cover.filter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface FluidFilter extends Filter<FluidStack, FluidFilter> {

    Map<ItemLike, Function<ItemStack, FluidFilter>> FILTERS = new HashMap<>();

    static void register(ItemLike filterItem, Function<ItemStack, FluidFilter> factory) {
        FILTERS.put(filterItem, factory);
    }

    static boolean isFilterItem(ItemStack itemStack) {
        return FILTERS.containsKey(itemStack.getItem());
    }

    static FluidFilter loadFilter(ItemStack itemStack) {
        Function<ItemStack, FluidFilter> factory = FILTERS.get(itemStack.getItem());
        if (factory == null) {
            return EMPTY;
        }
        return factory.apply(itemStack);
    }

    int testFluidAmount(FluidStack fluidStack);

    default boolean supportsAmounts() {
        return !isBlackList();
    }

    FluidFilter EMPTY = new FluidFilter() {

        @Override
        public boolean test(FluidStack fluidStack) {
            return true;
        }

        @Override
        public int testFluidAmount(FluidStack fluidStack) {
            return Integer.MAX_VALUE;
        }

        @Override
        public UIElement openConfigurator(int x, int y) {
            throw new UnsupportedOperationException("Not available for empty fluid filter");
        }

        @Override
        public void setOnUpdated(Consumer<FluidFilter> onUpdated) {
            throw new UnsupportedOperationException("Not available for empty fluid filter");
        }
    };
}
