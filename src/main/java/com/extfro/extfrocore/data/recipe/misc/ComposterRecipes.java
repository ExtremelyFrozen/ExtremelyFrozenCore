package com.extfro.extfrocore.data.recipe.misc;

import com.extfro.extfrocore.common.data.GTBlocks;

import net.minecraft.world.item.Item;

import java.util.function.BiConsumer;

public class ComposterRecipes {

    // Add composter things here.
    public static void addComposterRecipes(BiConsumer<Item, Float> provider) {
        provider.accept(GTBlocks.RUBBER_SAPLING.asItem(), 0.3F);
    }
}
