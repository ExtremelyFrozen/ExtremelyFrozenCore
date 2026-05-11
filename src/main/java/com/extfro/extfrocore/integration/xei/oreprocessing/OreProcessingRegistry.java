package com.extfro.extfrocore.integration.xei.oreprocessing;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class OreProcessingRegistry {

    public static final ResourceLocation DEFAULT_CATEGORY_ID = ResourceLocation.fromNamespaceAndPath(ExtForCore.MOD_ID,
            "ore_processing");
    public static final OreProcessingCategory DEFAULT_CATEGORY = OreProcessingCategory.builder(DEFAULT_CATEGORY_ID,
            Component.translatable("extfrocore.xei.ore_processing"))
            .icon(new ItemStack(Items.RAW_IRON))
            .build();

    private static final List<OreProcessingCategory> CATEGORIES = new ArrayList<>();
    private static final List<OreProcessingDataSource> DATA_SOURCES = new ArrayList<>();

    static {
        registerCategory(DEFAULT_CATEGORY);
    }

    private OreProcessingRegistry() {}

    public static void registerCategory(OreProcessingCategory category) {
        CATEGORIES.add(category);
    }

    public static void registerDataSource(OreProcessingDataSource dataSource) {
        DATA_SOURCES.add(dataSource);
    }

    public static void registerDisplays(Consumer<OreProcessingDisplay> consumer) {
        for (OreProcessingDataSource dataSource : DATA_SOURCES) {
            dataSource.collectDisplays(consumer);
        }
    }

    @Unmodifiable
    public static List<OreProcessingCategory> getCategories() {
        return List.copyOf(CATEGORIES);
    }

    @Unmodifiable
    public static List<OreProcessingDisplay> getDisplays() {
        List<OreProcessingDisplay> displays = new ArrayList<>();
        registerDisplays(displays::add);
        return List.copyOf(displays);
    }

    @Unmodifiable
    public static List<OreProcessingDisplay> getDisplays(OreProcessingCategory category) {
        return getDisplays().stream()
                .filter(display -> display.category().equals(category))
                .toList();
    }
}
