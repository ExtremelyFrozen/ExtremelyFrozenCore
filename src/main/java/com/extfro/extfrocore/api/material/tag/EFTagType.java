package com.extfro.extfrocore.api.material.tag;

import com.extfro.extfrocore.api.material.EFMaterial;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class EFTagType {

    private final String tagPath;
    private boolean parentTag;
    private BiFunction<EFTagPrefix, EFMaterial, TagKey<Item>> formatter;
    @Nullable
    private Predicate<EFMaterial> filter;

    private EFTagType(String tagPath) {
        this.tagPath = tagPath;
    }

    public static EFTagType withDefaultFormatter(String tagPath, boolean vanilla) {
        EFTagType type = new EFTagType(tagPath);
        type.formatter = Util.memoize((prefix, material) -> createItemTag(
                type.tagPath.formatted(material.getName()), vanilla));
        return type;
    }

    public static EFTagType withPrefixFormatter(String tagPath) {
        EFTagType type = new EFTagType(tagPath);
        type.formatter = Util.memoize((prefix, material) -> createItemTag(
                type.tagPath.formatted(prefix.getLowerCaseName(), material.getName()), false));
        return type;
    }

    public static EFTagType withPrefixOnlyFormatter(String tagPath) {
        EFTagType type = new EFTagType(tagPath);
        type.formatter = Util.memoize((prefix, material) -> createItemTag(
                type.tagPath.formatted(prefix.getLowerCaseName()), false));
        type.parentTag = true;
        return type;
    }

    public static EFTagType withNoFormatter(String tagPath, boolean vanilla) {
        EFTagType type = new EFTagType(tagPath);
        type.formatter = Util.memoize((prefix, material) -> createItemTag(type.tagPath, vanilla));
        type.parentTag = true;
        return type;
    }

    public static EFTagType withCustomFormatter(String tagPath,
                                                BiFunction<EFTagPrefix, EFMaterial, TagKey<Item>> formatter) {
        EFTagType type = new EFTagType(tagPath);
        type.formatter = Util.memoize(formatter);
        return type;
    }

    public static EFTagType withCustomFilter(String tagPath, boolean vanilla, Predicate<EFMaterial> filter) {
        EFTagType type = new EFTagType(tagPath);
        type.filter = filter;
        type.formatter = Util.memoize((prefix, material) -> createItemTag(type.tagPath, vanilla));
        return type;
    }

    public boolean isParentTag() {
        return parentTag;
    }

    @Nullable
    public TagKey<Item> getTag(EFTagPrefix prefix, @NotNull EFMaterial material) {
        if (filter != null && !material.isEmpty() && !filter.test(material)) {
            return null;
        }
        return formatter.apply(prefix, material);
    }

    private static TagKey<Item> createItemTag(String path, boolean vanilla) {
        ResourceLocation location = vanilla ?
                ResourceLocation.withDefaultNamespace(path) :
                ResourceLocation.fromNamespaceAndPath("c", path);
        return TagKey.create(Registries.ITEM, location);
    }
}
