package com.extfro.extfrocore.api.material.info;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record EFMaterialIconType(String name) {

    public static final Map<String, EFMaterialIconType> ICON_TYPES = new HashMap<>();

    public static final EFMaterialIconType dustTiny = new EFMaterialIconType("dustTiny");
    public static final EFMaterialIconType dustSmall = new EFMaterialIconType("dustSmall");
    public static final EFMaterialIconType dust = new EFMaterialIconType("dust");
    public static final EFMaterialIconType dustImpure = new EFMaterialIconType("dustImpure");
    public static final EFMaterialIconType dustPure = new EFMaterialIconType("dustPure");
    public static final EFMaterialIconType rawOre = new EFMaterialIconType("rawOre");
    public static final EFMaterialIconType rawOreBlock = new EFMaterialIconType("rawOreBlock");
    public static final EFMaterialIconType crushed = new EFMaterialIconType("crushed");
    public static final EFMaterialIconType crushedPurified = new EFMaterialIconType("crushedPurified");
    public static final EFMaterialIconType crushedRefined = new EFMaterialIconType("crushedRefined");
    public static final EFMaterialIconType gem = new EFMaterialIconType("gem");
    public static final EFMaterialIconType gemChipped = new EFMaterialIconType("gemChipped");
    public static final EFMaterialIconType gemFlawed = new EFMaterialIconType("gemFlawed");
    public static final EFMaterialIconType gemFlawless = new EFMaterialIconType("gemFlawless");
    public static final EFMaterialIconType gemExquisite = new EFMaterialIconType("gemExquisite");
    public static final EFMaterialIconType nugget = new EFMaterialIconType("nugget");
    public static final EFMaterialIconType ingot = new EFMaterialIconType("ingot");
    public static final EFMaterialIconType ingotHot = new EFMaterialIconType("ingotHot");
    public static final EFMaterialIconType ingotDouble = new EFMaterialIconType("ingotDouble");
    public static final EFMaterialIconType ingotTriple = new EFMaterialIconType("ingotTriple");
    public static final EFMaterialIconType ingotQuadruple = new EFMaterialIconType("ingotQuadruple");
    public static final EFMaterialIconType ingotQuintuple = new EFMaterialIconType("ingotQuintuple");
    public static final EFMaterialIconType plate = new EFMaterialIconType("plate");
    public static final EFMaterialIconType plateDouble = new EFMaterialIconType("plateDouble");
    public static final EFMaterialIconType plateTriple = new EFMaterialIconType("plateTriple");
    public static final EFMaterialIconType plateQuadruple = new EFMaterialIconType("plateQuadruple");
    public static final EFMaterialIconType plateQuintuple = new EFMaterialIconType("plateQuintuple");
    public static final EFMaterialIconType plateDense = new EFMaterialIconType("plateDense");
    public static final EFMaterialIconType rod = new EFMaterialIconType("rod");
    public static final EFMaterialIconType lens = new EFMaterialIconType("lens");
    public static final EFMaterialIconType round = new EFMaterialIconType("round");
    public static final EFMaterialIconType bolt = new EFMaterialIconType("bolt");
    public static final EFMaterialIconType screw = new EFMaterialIconType("screw");
    public static final EFMaterialIconType ring = new EFMaterialIconType("ring");
    public static final EFMaterialIconType wireFine = new EFMaterialIconType("wireFine");
    public static final EFMaterialIconType gearSmall = new EFMaterialIconType("gearSmall");
    public static final EFMaterialIconType rotor = new EFMaterialIconType("rotor");
    public static final EFMaterialIconType rodLong = new EFMaterialIconType("rodLong");
    public static final EFMaterialIconType springSmall = new EFMaterialIconType("springSmall");
    public static final EFMaterialIconType spring = new EFMaterialIconType("spring");
    public static final EFMaterialIconType gear = new EFMaterialIconType("gear");
    public static final EFMaterialIconType foil = new EFMaterialIconType("foil");
    public static final EFMaterialIconType toolHeadSword = new EFMaterialIconType("toolHeadSword");
    public static final EFMaterialIconType toolHeadPickaxe = new EFMaterialIconType("toolHeadPickaxe");
    public static final EFMaterialIconType toolHeadShovel = new EFMaterialIconType("toolHeadShovel");
    public static final EFMaterialIconType toolHeadAxe = new EFMaterialIconType("toolHeadAxe");
    public static final EFMaterialIconType toolHeadHoe = new EFMaterialIconType("toolHeadHoe");
    public static final EFMaterialIconType toolHeadHammer = new EFMaterialIconType("toolHeadHammer");
    public static final EFMaterialIconType toolHeadFile = new EFMaterialIconType("toolHeadFile");
    public static final EFMaterialIconType toolHeadSaw = new EFMaterialIconType("toolHeadSaw");
    public static final EFMaterialIconType toolHeadBuzzSaw = new EFMaterialIconType("toolHeadBuzzSaw");
    public static final EFMaterialIconType toolHeadDrill = new EFMaterialIconType("toolHeadDrill");
    public static final EFMaterialIconType toolHeadChainsaw = new EFMaterialIconType("toolHeadChainsaw");
    public static final EFMaterialIconType toolHeadScythe = new EFMaterialIconType("toolHeadScythe");
    public static final EFMaterialIconType toolHeadScrewdriver = new EFMaterialIconType("toolHeadScrewdriver");
    public static final EFMaterialIconType toolHeadWrench = new EFMaterialIconType("toolHeadWrench");
    public static final EFMaterialIconType toolHeadWireCutter = new EFMaterialIconType("toolHeadWireCutter");
    public static final EFMaterialIconType turbineBlade = new EFMaterialIconType("turbineBlade");
    public static final EFMaterialIconType block = new EFMaterialIconType("block");
    public static final EFMaterialIconType ore = new EFMaterialIconType("ore");
    public static final EFMaterialIconType oreEmissive = new EFMaterialIconType("oreEmissive");
    public static final EFMaterialIconType oreSmall = new EFMaterialIconType("oreSmall");
    public static final EFMaterialIconType frame = new EFMaterialIconType("frame");
    public static final EFMaterialIconType wire = new EFMaterialIconType("wire");
    public static final EFMaterialIconType liquid = new EFMaterialIconType("liquid");
    public static final EFMaterialIconType gas = new EFMaterialIconType("gas");
    public static final EFMaterialIconType plasma = new EFMaterialIconType("plasma");
    public static final EFMaterialIconType molten = new EFMaterialIconType("molten");
    public static final EFMaterialIconType seed = new EFMaterialIconType("seed");
    public static final EFMaterialIconType crop = new EFMaterialIconType("crop");
    public static final EFMaterialIconType essence = new EFMaterialIconType("essence");

    private static final Table<EFMaterialIconType, EFMaterialIconSet, ResourceLocation> ITEM_MODEL_CACHE = HashBasedTable.create();
    private static final Table<EFMaterialIconType, EFMaterialIconSet, ResourceLocation> ITEM_TEXTURE_CACHE = HashBasedTable.create();
    private static final Table<EFMaterialIconType, EFMaterialIconSet, ResourceLocation> ITEM_TEXTURE_CACHE_SECONDARY = HashBasedTable.create();
    private static final Table<EFMaterialIconType, EFMaterialIconSet, ResourceLocation> BLOCK_MODEL_CACHE = HashBasedTable.create();
    private static final Table<EFMaterialIconType, EFMaterialIconSet, ResourceLocation> BLOCK_TEXTURE_CACHE = HashBasedTable.create();
    private static final Table<EFMaterialIconType, EFMaterialIconSet, ResourceLocation> BLOCK_TEXTURE_CACHE_SECONDARY = HashBasedTable.create();

    public EFMaterialIconType(String name) {
        this.name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        Preconditions.checkArgument(!ICON_TYPES.containsKey(this.name),
                "MaterialIconType " + this.name + " already registered");
        ICON_TYPES.put(this.name, this);
    }

    public static EFMaterialIconType getByName(String name) {
        return ICON_TYPES.get(name);
    }

    @NotNull
    public ResourceLocation getBlockModelPath(@NotNull EFMaterialIconSet iconSet, boolean readCache) {
        if (readCache && BLOCK_MODEL_CACHE.contains(this, iconSet)) {
            return BLOCK_MODEL_CACHE.get(this, iconSet);
        }
        ResourceLocation location = ExtForCore.id("block/material_sets/%s/%s".formatted(
                resolveModelIconSet("block", iconSet).name, this.name));
        BLOCK_MODEL_CACHE.put(this, iconSet, location);
        return location;
    }

    @NotNull
    public ResourceLocation getItemModelPath(@NotNull EFMaterialIconSet iconSet, boolean readCache) {
        if (readCache && ITEM_MODEL_CACHE.contains(this, iconSet)) {
            return ITEM_MODEL_CACHE.get(this, iconSet);
        }
        ResourceLocation location = ExtForCore.id("item/material_sets/%s/%s".formatted(
                resolveModelIconSet("item", iconSet).name, this.name));
        ITEM_MODEL_CACHE.put(this, iconSet, location);
        return location;
    }

    @Nullable
    public ResourceLocation getItemTexturePath(@NotNull EFMaterialIconSet iconSet, boolean readCache) {
        return getItemTexturePath(iconSet, null, readCache);
    }

    @Nullable
    public ResourceLocation getItemTexturePath(@NotNull EFMaterialIconSet iconSet, @Nullable String suffix,
                                               boolean readCache) {
        boolean secondary = suffix != null && !suffix.isBlank();
        Table<EFMaterialIconType, EFMaterialIconSet, ResourceLocation> cache = secondary ?
                ITEM_TEXTURE_CACHE_SECONDARY : ITEM_TEXTURE_CACHE;
        if (readCache && cache.contains(this, iconSet)) {
            return cache.get(this, iconSet);
        }
        EFMaterialIconSet resolvedIconSet = resolveTextureIconSet("item", iconSet, suffix);
        if (secondary && !resourceExists(textureResource("item", resolvedIconSet, suffix))) {
            return null;
        }
        ResourceLocation location = ExtForCore.id("item/material_sets/%s/%s%s".formatted(
                resolvedIconSet.name, this.name, suffix(secondary, suffix)));
        cache.put(this, iconSet, location);
        return location;
    }

    @NotNull
    public List<ResourceLocation> getItemTextureCandidates(@NotNull EFMaterialIconSet iconSet) {
        return getItemTextureCandidates(iconSet, null);
    }

    @NotNull
    public List<ResourceLocation> getItemTextureCandidates(@NotNull EFMaterialIconSet iconSet,
                                                           @Nullable String suffix) {
        return getTextureCandidates("item", iconSet, suffix);
    }

    @NotNull
    public ResourceLocation getBlockTexturePath(@NotNull EFMaterialIconSet iconSet, boolean readCache) {
        return getBlockTexturePath(iconSet, null, readCache);
    }

    @NotNull
    public ResourceLocation getBlockTexturePath(@NotNull EFMaterialIconSet iconSet, @Nullable String suffix,
                                                boolean readCache) {
        boolean secondary = suffix != null && !suffix.isBlank();
        Table<EFMaterialIconType, EFMaterialIconSet, ResourceLocation> cache = secondary ?
                BLOCK_TEXTURE_CACHE_SECONDARY : BLOCK_TEXTURE_CACHE;
        if (readCache && cache.contains(this, iconSet)) {
            return cache.get(this, iconSet);
        }
        ResourceLocation location = ExtForCore.id("block/material_sets/%s/%s%s".formatted(
                resolveTextureIconSet("block", iconSet, suffix).name, this.name, suffix(secondary, suffix)));
        cache.put(this, iconSet, location);
        return location;
    }

    @NotNull
    public List<ResourceLocation> getBlockTextureCandidates(@NotNull EFMaterialIconSet iconSet) {
        return getBlockTextureCandidates(iconSet, null);
    }

    @NotNull
    public List<ResourceLocation> getBlockTextureCandidates(@NotNull EFMaterialIconSet iconSet,
                                                            @Nullable String suffix) {
        return getTextureCandidates("block", iconSet, suffix);
    }

    @NotNull
    private List<ResourceLocation> getTextureCandidates(@NotNull String type, @NotNull EFMaterialIconSet iconSet,
                                                        @Nullable String suffix) {
        boolean secondary = suffix != null && !suffix.isBlank();
        List<ResourceLocation> candidates = new ArrayList<>();
        EFMaterialIconSet current = iconSet;
        while (current != null) {
            candidates.add(ExtForCore.id("%s/material_sets/%s/%s%s".formatted(
                    type, current.name, this.name, suffix(secondary, suffix))));
            if (current.isRootIconset) {
                break;
            }
            current = current.parentIconset;
        }
        return candidates;
    }

    @NotNull
    private EFMaterialIconSet resolveModelIconSet(@NotNull String type, @NotNull EFMaterialIconSet iconSet) {
        if (canCheckResources()) {
            EFMaterialIconSet current = iconSet;
            while (current != null) {
                if (resourceExists(modelResource(type, current))) {
                    return current;
                }
                if (current.isRootIconset) {
                    break;
                }
                current = current.parentIconset;
            }
        }
        return iconSet;
    }

    @NotNull
    private EFMaterialIconSet resolveTextureIconSet(@NotNull String type, @NotNull EFMaterialIconSet iconSet,
                                                    @Nullable String suffix) {
        if (canCheckResources()) {
            EFMaterialIconSet current = iconSet;
            while (current != null) {
                if (resourceExists(textureResource(type, current, suffix))) {
                    return current;
                }
                if (current.isRootIconset) {
                    break;
                }
                current = current.parentIconset;
            }
        }
        return iconSet;
    }

    private ResourceLocation modelResource(@NotNull String type, @NotNull EFMaterialIconSet iconSet) {
        return ExtForCore.id("models/%s/material_sets/%s/%s.json".formatted(type, iconSet.name, this.name));
    }

    private ResourceLocation textureResource(@NotNull String type, @NotNull EFMaterialIconSet iconSet,
                                             @Nullable String suffix) {
        boolean secondary = suffix != null && !suffix.isBlank();
        return ExtForCore.id("textures/%s/material_sets/%s/%s%s.png".formatted(
                type, iconSet.name, this.name, suffix(secondary, suffix)));
    }

    private static boolean canCheckResources() {
        return ExtForCore.isClientSide() && Minecraft.getInstance() != null &&
                Minecraft.getInstance().getResourceManager() != null;
    }

    private static boolean resourceExists(ResourceLocation location) {
        return canCheckResources() && Minecraft.getInstance().getResourceManager().getResource(location).isPresent();
    }

    @NotNull
    private static String suffix(boolean secondary, @Nullable String suffix) {
        return secondary ? "_" + suffix : "";
    }
}
