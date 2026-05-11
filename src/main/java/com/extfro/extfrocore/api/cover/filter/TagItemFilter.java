package com.extfro.extfrocore.api.cover.filter;

import com.extfro.extfrocore.api.sync_system.SyncedComponents;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

public class TagItemFilter extends TagFilter<ItemStack, ItemFilter> implements ItemFilter {

    private final Object2BooleanMap<Item> cache = new Object2BooleanOpenHashMap<>();

    public TagItemFilter(String filterExpr) {
        setFilterExpr(filterExpr);
    }

    public static TagItemFilter loadFilter(ItemStack itemStack) {
        String expr = itemStack.getOrDefault(SyncedComponents.TAG_FILTER_EXPRESSION.get(), "");
        TagItemFilter filter = new TagItemFilter(expr);
        filter.itemWriter = updated -> itemStack.set(SyncedComponents.TAG_FILTER_EXPRESSION.get(),
                ((TagItemFilter) updated).tagFilterExpression);
        return filter;
    }

    @Override
    public void setFilterExpr(String filterExpr) {
        cache.clear();
        super.setFilterExpr(filterExpr);
    }

    @Override
    public boolean test(ItemStack itemStack) {
        if (tagFilterExpression.isEmpty()) {
            return false;
        }
        if (cache.containsKey(itemStack.getItem())) {
            return cache.getOrDefault(itemStack.getItem(), false);
        }
        boolean result = TagExpressionFilter.tagsMatch(matchExpr, itemStack);
        cache.put(itemStack.getItem(), result);
        return result;
    }

    @Override
    public int testItemCount(ItemStack itemStack) {
        return test(itemStack) ? Integer.MAX_VALUE : 0;
    }

    @Override
    public boolean supportsAmounts() {
        return false;
    }
}
