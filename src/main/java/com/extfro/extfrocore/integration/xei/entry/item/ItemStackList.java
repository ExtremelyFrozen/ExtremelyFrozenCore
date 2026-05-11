package com.extfro.extfrocore.integration.xei.entry.item;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public final class ItemStackList implements ItemEntryList {

    private final List<ItemStack> stacks;

    public ItemStackList() {
        this.stacks = new ArrayList<>();
    }

    public ItemStackList(List<ItemStack> stacks) {
        this.stacks = new ArrayList<>(stacks);
    }

    public static ItemStackList of(ItemStack stack) {
        ItemStackList list = new ItemStackList();
        list.add(stack);
        return list;
    }

    public static ItemStackList of(Collection<ItemStack> stacks) {
        ItemStackList list = new ItemStackList();
        list.addAll(stacks);
        return list;
    }

    public void add(ItemStack stack) {
        stacks.add(stack);
    }

    public void addAll(Collection<ItemStack> stacks) {
        this.stacks.addAll(stacks);
    }

    @Override
    public List<ItemStack> getStacks() {
        return stacks;
    }

    @Override
    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    public Stream<ItemStack> stream() {
        return stacks.stream();
    }
}
