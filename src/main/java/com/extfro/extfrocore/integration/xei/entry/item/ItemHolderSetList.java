package com.extfro.extfrocore.integration.xei.entry.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ItemHolderSetList implements ItemEntryList {

    @Getter
    private final List<ItemHolderSetEntry> entries = new ArrayList<>();

    public static ItemHolderSetList of(@NotNull Holder<Item> item, int amount, @NotNull DataComponentPatch patch) {
        ItemHolderSetList list = new ItemHolderSetList();
        list.add(item, amount, patch);
        return list;
    }

    public void add(ItemHolderSetEntry entry) {
        entries.add(entry);
    }

    public void add(@NotNull Holder<Item> item, int amount, @NotNull DataComponentPatch patch) {
        add(new ItemHolderSetEntry(item, amount, patch));
    }

    @Override
    public List<ItemStack> getStacks() {
        return entries.stream().flatMap(ItemHolderSetEntry::stacks).toList();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public record ItemHolderSetEntry(@NotNull Holder<Item> item, int amount, @NotNull DataComponentPatch patch) {

        public Stream<ItemStack> stacks() {
            return Stream.of(new ItemStack(item, amount, patch));
        }
    }
}
