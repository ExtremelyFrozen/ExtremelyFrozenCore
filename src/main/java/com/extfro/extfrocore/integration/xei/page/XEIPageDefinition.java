package com.extfro.extfrocore.integration.xei.page;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public record XEIPageDefinition<T>(
        ResourceLocation id,
        Component title,
        ItemStack icon,
        Class<T> displayType,
        int width,
        int height,
        Supplier<? extends List<? extends T>> displaySupplier,
        Supplier<? extends List<ItemStack>> catalystSupplier)
        implements XEIPage {

    public XEIPageDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(displayType, "displayType");
        Objects.requireNonNull(displaySupplier, "displaySupplier");
        Objects.requireNonNull(catalystSupplier, "catalystSupplier");
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be positive");
        }
        icon = icon.copy();
    }

    public static <T> Builder<T> builder(ResourceLocation id, Component title, Class<T> displayType) {
        return new Builder<>(id, title, displayType);
    }

    @Unmodifiable
    public List<T> displays() {
        return List.copyOf(displaySupplier.get());
    }

    @Unmodifiable
    public List<ItemStack> catalysts() {
        List<ItemStack> catalysts = new ArrayList<>();
        for (ItemStack catalyst : catalystSupplier.get()) {
            if (!catalyst.isEmpty()) {
                catalysts.add(catalyst.copy());
            }
        }
        return List.copyOf(catalysts);
    }

    public static final class Builder<T> {

        private final ResourceLocation id;
        private final Component title;
        private final Class<T> displayType;
        private ItemStack icon = ItemStack.EMPTY;
        private int width = 176;
        private int height = 96;
        private Supplier<? extends List<? extends T>> displaySupplier = List::of;
        private Supplier<? extends List<ItemStack>> catalystSupplier = List::of;

        private Builder(ResourceLocation id, Component title, Class<T> displayType) {
            this.id = id;
            this.title = title;
            this.displayType = displayType;
        }

        public Builder<T> icon(ItemStack icon) {
            this.icon = icon.copy();
            return this;
        }

        public Builder<T> size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder<T> displays(Supplier<? extends List<? extends T>> displaySupplier) {
            this.displaySupplier = displaySupplier;
            return this;
        }

        public Builder<T> catalysts(Supplier<? extends List<ItemStack>> catalystSupplier) {
            this.catalystSupplier = catalystSupplier;
            return this;
        }

        public XEIPageDefinition<T> build() {
            return new XEIPageDefinition<>(id, title, icon, displayType, width, height, displaySupplier,
                    catalystSupplier);
        }
    }
}
