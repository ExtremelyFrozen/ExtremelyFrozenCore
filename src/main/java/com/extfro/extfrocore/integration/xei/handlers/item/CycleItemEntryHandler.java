package com.extfro.extfrocore.integration.xei.handlers.item;

import com.extfro.extfrocore.integration.xei.entry.item.ItemEntryList;
import com.extfro.extfrocore.integration.xei.entry.item.ItemStackList;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CycleItemEntryHandler implements IItemHandlerModifiable {

    @Getter
    private final List<ItemEntryList> entries;

    @Nullable
    private List<List<ItemStack>> unwrapped;

    public CycleItemEntryHandler(List<ItemEntryList> entries) {
        this.entries = new ArrayList<>(entries);
    }

    public static CycleItemEntryHandler createFromStacks(List<List<ItemStack>> stacks) {
        List<ItemEntryList> entries = new ArrayList<>();
        for (List<ItemStack> list : stacks) {
            entries.add(ItemStackList.of(list));
        }
        CycleItemEntryHandler handler = new CycleItemEntryHandler(entries);
        handler.unwrapped = stacks;
        return handler;
    }

    public List<List<ItemStack>> getUnwrapped() {
        if (unwrapped == null) {
            unwrapped = entries.stream()
                    .map(CycleItemEntryHandler::getStacksNullable)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return unwrapped;
    }

    @Nullable
    private static List<ItemStack> getStacksNullable(@Nullable ItemEntryList list) {
        return list == null ? null : list.getStacks();
    }

    public ItemEntryList getEntry(int index) {
        return entries.get(index);
    }

    @Override
    public int getSlots() {
        return entries.size();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        List<ItemStack> stackList = getUnwrapped().get(slot);
        if (stackList == null || stackList.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int index = Math.abs((int) (System.currentTimeMillis() / 1000) % stackList.size());
        return stackList.get(index);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (slot >= 0 && slot < entries.size()) {
            entries.set(slot, ItemStackList.of(stack));
            unwrapped = null;
        }
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }
}
