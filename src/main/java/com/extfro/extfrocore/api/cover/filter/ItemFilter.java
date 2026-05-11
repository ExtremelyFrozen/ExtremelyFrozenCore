package com.extfro.extfrocore.api.cover.filter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ItemFilter extends Filter<ItemStack, ItemFilter> {

    Map<ItemLike, Function<ItemStack, ItemFilter>> FILTERS = new HashMap<>();

    static void register(ItemLike filterItem, Function<ItemStack, ItemFilter> factory) {
        FILTERS.put(filterItem, factory);
    }

    static boolean isFilterItem(ItemStack itemStack) {
        return FILTERS.containsKey(itemStack.getItem());
    }

    static ItemFilter loadFilter(ItemStack itemStack) {
        Function<ItemStack, ItemFilter> factory = FILTERS.get(itemStack.getItem());
        if (factory == null) {
            return EMPTY;
        }
        return factory.apply(itemStack);
    }

    int testItemCount(ItemStack itemStack);

    default boolean supportsAmounts() {
        return !isBlackList();
    }

    ItemFilter EMPTY = new ItemFilter() {

        @Override
        public int testItemCount(ItemStack itemStack) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean test(ItemStack itemStack) {
            return true;
        }

        @Override
        public UIElement openConfigurator(int x, int y) {
            throw new UnsupportedOperationException("Not available for empty item filter");
        }

        @Override
        public void setOnUpdated(Consumer<ItemFilter> onUpdated) {
            throw new UnsupportedOperationException("Not available for empty item filter");
        }
    };
}
