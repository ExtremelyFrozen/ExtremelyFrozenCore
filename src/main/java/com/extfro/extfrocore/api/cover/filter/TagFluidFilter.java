package com.extfro.extfrocore.api.cover.filter;

import com.extfro.extfrocore.api.sync_system.SyncedComponents;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

public class TagFluidFilter extends TagFilter<FluidStack, FluidFilter> implements FluidFilter {

    private final Object2BooleanMap<Fluid> cache = new Object2BooleanOpenHashMap<>();

    public TagFluidFilter(String filterExpr) {
        setFilterExpr(filterExpr);
    }

    public static TagFluidFilter loadFilter(ItemStack itemStack) {
        String expr = itemStack.getOrDefault(SyncedComponents.TAG_FILTER_EXPRESSION.get(), "");
        TagFluidFilter filter = new TagFluidFilter(expr);
        filter.itemWriter = updated -> itemStack.set(SyncedComponents.TAG_FILTER_EXPRESSION.get(),
                ((TagFluidFilter) updated).tagFilterExpression);
        return filter;
    }

    @Override
    public void setFilterExpr(String filterExpr) {
        cache.clear();
        super.setFilterExpr(filterExpr);
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        if (tagFilterExpression.isEmpty()) {
            return false;
        }
        if (cache.containsKey(fluidStack.getFluid())) {
            return cache.getOrDefault(fluidStack.getFluid(), false);
        }
        boolean result = TagExpressionFilter.tagsMatch(matchExpr, fluidStack);
        cache.put(fluidStack.getFluid(), result);
        return result;
    }

    @Override
    public int testFluidAmount(FluidStack fluidStack) {
        return test(fluidStack) ? Integer.MAX_VALUE : 0;
    }

    @Override
    public boolean supportsAmounts() {
        return false;
    }
}
