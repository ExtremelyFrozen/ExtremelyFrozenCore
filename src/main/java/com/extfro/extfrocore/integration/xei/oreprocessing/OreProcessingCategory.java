package com.extfro.extfrocore.integration.xei.oreprocessing;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record OreProcessingCategory(ResourceLocation id, Component title, ItemStack icon, int width, int height,
                                    List<ItemStack> catalysts) {

    public OreProcessingCategory {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be positive");
        }
        icon = icon.copy();
        catalysts = copyStacks(catalysts);
    }

    public static Builder builder(ResourceLocation id, Component title) {
        return new Builder(id, title);
    }

    private static List<ItemStack> copyStacks(Collection<ItemStack> stacks) {
        List<ItemStack> copy = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            copy.add(stack.copy());
        }
        return List.copyOf(copy);
    }

    public static final class Builder {

        private final ResourceLocation id;
        private final Component title;
        private ItemStack icon = ItemStack.EMPTY;
        private int width = 176;
        private int height = 166;
        private final List<ItemStack> catalysts = new ArrayList<>();

        private Builder(ResourceLocation id, Component title) {
            this.id = id;
            this.title = title;
        }

        public Builder icon(ItemStack icon) {
            this.icon = icon.copy();
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder catalyst(ItemStack catalyst) {
            this.catalysts.add(catalyst.copy());
            return this;
        }

        public Builder catalysts(Collection<ItemStack> catalysts) {
            for (ItemStack catalyst : catalysts) {
                catalyst(catalyst);
            }
            return this;
        }

        public OreProcessingCategory build() {
            return new OreProcessingCategory(id, title, icon, width, height, catalysts);
        }
    }
}
