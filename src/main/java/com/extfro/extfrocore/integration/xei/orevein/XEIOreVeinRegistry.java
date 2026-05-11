package com.extfro.extfrocore.integration.xei.orevein;

import com.extfro.extfrocore.api.registry.EFRegistries;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

public final class XEIOreVeinRegistry {

    private XEIOreVeinRegistry() {}

    @Unmodifiable
    public static List<XEIOreVeinDisplay.OreVein> getOreVeinDisplays() {
        return collect(EFRegistries.ORE_VEIN_REGISTRY, XEIOreVeinDisplay.OreVein::of).stream()
                .filter(XEIOreVeinDisplay.OreVein::canGenerate)
                .toList();
    }

    @Unmodifiable
    public static List<XEIOreVeinDisplay.BedrockOreVein> getBedrockOreVeinDisplays() {
        return collect(EFRegistries.BEDROCK_ORE_REGISTRY, XEIOreVeinDisplay.BedrockOreVein::of).stream()
                .filter(XEIOreVeinDisplay.BedrockOreVein::canGenerate)
                .toList();
    }

    @Unmodifiable
    public static List<XEIOreVeinDisplay.BedrockFluidVein> getBedrockFluidVeinDisplays() {
        return collect(EFRegistries.BEDROCK_FLUID_REGISTRY, XEIOreVeinDisplay.BedrockFluidVein::of).stream()
                .filter(XEIOreVeinDisplay.BedrockFluidVein::canGenerate)
                .toList();
    }

    @Unmodifiable
    public static List<XEIOreVeinDisplay> getAllDisplays() {
        List<XEIOreVeinDisplay> displays = new ArrayList<>();
        displays.addAll(getOreVeinDisplays());
        displays.addAll(getBedrockOreVeinDisplays());
        displays.addAll(getBedrockFluidVeinDisplays());
        return List.copyOf(displays);
    }

    private static <T, D extends XEIOreVeinDisplay> List<D> collect(ResourceKey<Registry<T>> registryKey,
                                                                    BiFunction<ResourceLocation, T, D> factory) {
        return EFRegistries.builtinRegistry().registry(registryKey)
                .map(registry -> registry.holders()
                        .map(holder -> factory.apply(id(holder), holder.value()))
                        .sorted(Comparator.comparing(display -> display.id().toString()))
                        .toList())
                .orElse(List.of());
    }

    private static <T> ResourceLocation id(Holder<T> holder) {
        return holder.unwrapKey()
                .map(ResourceKey::location)
                .orElseThrow(() -> new IllegalStateException("Cannot create XEI ore vein display for unbound holder"));
    }
}
