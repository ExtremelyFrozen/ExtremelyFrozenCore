package com.extfro.extfrocore.integration.xei.entry.item;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public sealed interface ItemEntryList permits ItemHolderSetList, ItemStackList, ItemTagList {

    List<ItemStack> getStacks();

    boolean isEmpty();
}
