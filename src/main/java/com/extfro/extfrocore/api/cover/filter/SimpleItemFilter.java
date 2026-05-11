package com.extfro.extfrocore.api.cover.filter;

import com.extfro.extfrocore.api.sync_system.SyncedComponents;

import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SimpleItemFilter implements ItemFilter {

    public static final Codec<SimpleItemFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("is_blacklist").forGetter(SimpleItemFilter::isBlackList),
            Codec.BOOL.fieldOf("ignore_components").forGetter(SimpleItemFilter::isIgnoreComponents),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("matches")
                    .forGetter(filter -> Arrays.stream(filter.matches).toList()))
            .apply(instance, SimpleItemFilter::new));

    @Getter
    protected boolean isBlackList;
    @Getter
    protected boolean ignoreComponents;
    @Getter
    protected ItemStack[] matches = new ItemStack[9];
    @Getter
    protected int maxStackSize = 1;

    protected Consumer<SimpleItemFilter> itemWriter = filter -> {};
    protected Consumer<SimpleItemFilter> onUpdated = filter -> itemWriter.accept(filter);

    public SimpleItemFilter() {
        Arrays.fill(matches, ItemStack.EMPTY);
    }

    public SimpleItemFilter(boolean isBlackList, boolean ignoreComponents, List<ItemStack> matches) {
        this();
        this.isBlackList = isBlackList;
        this.ignoreComponents = ignoreComponents;
        for (int i = 0; i < Math.min(this.matches.length, matches.size()); i++) {
            this.matches[i] = matches.get(i);
        }
    }

    public static SimpleItemFilter loadFilter(ItemStack itemStack) {
        SimpleItemFilter filter = itemStack.getOrDefault(SyncedComponents.SIMPLE_ITEM_FILTER.get(),
                new SimpleItemFilter());
        filter.itemWriter = updated -> itemStack.set(SyncedComponents.SIMPLE_ITEM_FILTER.get(), updated);
        return filter;
    }

    @Override
    public void setOnUpdated(Consumer<ItemFilter> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }

    @Override
    public boolean isBlank() {
        return !isBlackList && !ignoreComponents && Arrays.stream(matches).allMatch(ItemStack::isEmpty);
    }

    public void setBlackList(boolean blackList) {
        isBlackList = blackList;
        onUpdated.accept(this);
    }

    public void setIgnoreComponents(boolean ignoreComponents) {
        this.ignoreComponents = ignoreComponents;
        onUpdated.accept(this);
    }

    public void setMatch(int slot, ItemStack itemStack) {
        if (slot < 0 || slot >= matches.length) {
            throw new IndexOutOfBoundsException(slot);
        }
        matches[slot] = itemStack.copyWithCount(Math.min(itemStack.getCount(), maxStackSize));
        onUpdated.accept(this);
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
        for (ItemStack match : matches) {
            match.setCount(Math.min(match.getCount(), maxStackSize));
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
    public boolean test(ItemStack itemStack) {
        return testItemCount(itemStack) > 0;
    }

    @Override
    public int testItemCount(ItemStack itemStack) {
        int totalItemCount = getTotalConfiguredItemCount(itemStack);
        if (isBlackList) {
            return totalItemCount > 0 ? 0 : Integer.MAX_VALUE;
        }
        return totalItemCount;
    }

    public int getTotalConfiguredItemCount(ItemStack itemStack) {
        int totalCount = 0;
        for (ItemStack candidate : matches) {
            if (ignoreComponents) {
                if (ItemStack.isSameItem(candidate, itemStack)) {
                    totalCount += candidate.getCount();
                }
            } else if (ItemStack.isSameItemSameComponents(candidate, itemStack)) {
                totalCount += candidate.getCount();
            }
        }
        return totalCount;
    }
}
