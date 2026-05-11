package com.extfro.extfrocore.api.material.tag;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.material.EFMaterialStack;
import com.extfro.extfrocore.api.material.info.EFMaterialIconType;
import com.extfro.extfrocore.api.registry.EFRegistries;
import com.extfro.extfrocore.utils.FormattingUtil;

import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EFTagPrefix {

    public static final EFTagPrefix NULL_PREFIX = new EFTagPrefix("null", false);

    private final String name;
    private final List<EFTagType> tags = new ArrayList<>();
    private final Map<EFMaterial, Collection<Supplier<? extends ItemLike>>> ignoredMaterials = new HashMap<>();
    private final Map<EFMaterial, Float> materialAmounts = new HashMap<>();
    private final List<EFMaterialStack> secondaryMaterials = new ArrayList<>();
    private final Set<TagKey<Block>> miningToolTags = new HashSet<>();

    private String idPattern;
    private String langValue;
    private long materialAmount = -1;
    private boolean unificationEnabled;
    private boolean generateRecycling;
    private boolean generateItem;
    private boolean generateBlock;
    @Nullable
    private Predicate<EFMaterial> generationCondition;
    @Nullable
    private EFMaterialIconType materialIconType;
    @Nullable
    private BiConsumer<EFMaterial, List<Component>> tooltip;
    private int maxStackSize = 64;

    public EFTagPrefix(String name) {
        this(name, true);
    }

    protected EFTagPrefix(String name, boolean register) {
        this.name = Objects.requireNonNull(name, "name");
        String lowerCaseName = getLowerCaseName();
        this.idPattern = "%s_" + lowerCaseName;
        this.langValue = "%s " + toEnglishName(lowerCaseName);
        if (register) {
            EFRegistries.register(EFRegistries.TAG_PREFIXES, ExtForCore.id(lowerCaseName), this);
        }
    }

    @Nullable
    public static EFTagPrefix get(String name) {
        return EFRegistries.TAG_PREFIXES.get(ExtForCore.id(FormattingUtil.toLowerCaseUnderscore(name)));
    }

    public static Iterable<EFTagPrefix> values() {
        return EFRegistries.TAG_PREFIXES;
    }

    public boolean isEmpty() {
        return this == NULL_PREFIX;
    }

    public String getName() {
        return name;
    }

    public String getLowerCaseName() {
        return FormattingUtil.toLowerCaseUnderscore(name);
    }

    public String getIdPattern() {
        return idPattern;
    }

    public EFTagPrefix idPattern(String idPattern) {
        this.idPattern = idPattern;
        return this;
    }

    public String getLangValue() {
        return langValue;
    }

    public EFTagPrefix langValue(String langValue) {
        this.langValue = langValue;
        return this;
    }

    public long getMaterialAmount(@NotNull EFMaterial material) {
        if (material.isEmpty() || !isAmountModified(material)) {
            return materialAmount;
        }
        return (long) (EFMaterialTags.UNIT * materialAmounts.get(material));
    }

    public EFTagPrefix materialAmount(long materialAmount) {
        this.materialAmount = materialAmount;
        return this;
    }

    public boolean isUnificationEnabled() {
        return unificationEnabled;
    }

    public EFTagPrefix unificationEnabled(boolean unificationEnabled) {
        this.unificationEnabled = unificationEnabled;
        return this;
    }

    public boolean isGenerateRecycling() {
        return generateRecycling;
    }

    public EFTagPrefix generateRecycling(boolean generateRecycling) {
        this.generateRecycling = generateRecycling;
        return this;
    }

    public EFTagPrefix enableRecycling() {
        return generateRecycling(true);
    }

    public boolean doGenerateItem() {
        return generateItem;
    }

    public boolean doGenerateItem(EFMaterial material) {
        return generateItem && !isIgnored(material) &&
                (generationCondition == null || generationCondition.test(material));
    }

    public EFTagPrefix generateItem(boolean generateItem) {
        this.generateItem = generateItem;
        return this;
    }

    public boolean doGenerateBlock() {
        return generateBlock;
    }

    public boolean doGenerateBlock(EFMaterial material) {
        return generateBlock && !isIgnored(material) &&
                (generationCondition == null || generationCondition.test(material));
    }

    public EFTagPrefix generateBlock(boolean generateBlock) {
        this.generateBlock = generateBlock;
        return this;
    }

    @Nullable
    public Predicate<EFMaterial> getGenerationCondition() {
        return generationCondition;
    }

    public EFTagPrefix generationCondition(@Nullable Predicate<EFMaterial> generationCondition) {
        this.generationCondition = generationCondition;
        return this;
    }

    @Nullable
    public EFMaterialIconType materialIconType() {
        return materialIconType;
    }

    public EFTagPrefix materialIconType(@Nullable EFMaterialIconType materialIconType) {
        this.materialIconType = materialIconType;
        return this;
    }

    @Nullable
    public BiConsumer<EFMaterial, List<Component>> tooltip() {
        return tooltip;
    }

    public EFTagPrefix tooltip(@Nullable BiConsumer<EFMaterial, List<Component>> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public EFTagPrefix maxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
        return this;
    }

    @Unmodifiable
    public List<EFMaterialStack> getSecondaryMaterials() {
        return Collections.unmodifiableList(secondaryMaterials);
    }

    public EFTagPrefix addSecondaryMaterial(EFMaterialStack secondaryMaterial) {
        secondaryMaterials.add(Objects.requireNonNull(secondaryMaterial, "secondaryMaterial"));
        return this;
    }

    @Unmodifiable
    public Set<TagKey<Block>> getMiningToolTags() {
        return Collections.unmodifiableSet(miningToolTags);
    }

    public EFTagPrefix miningToolTag(TagKey<Block> tag) {
        miningToolTags.add(tag);
        return this;
    }

    public EFTagPrefix defaultTagPath(String path) {
        return defaultTagPath(path, false);
    }

    public EFTagPrefix defaultTagPath(String path, boolean vanilla) {
        tags.add(EFTagType.withDefaultFormatter(path, vanilla));
        return this;
    }

    public EFTagPrefix prefixTagPath(String path) {
        tags.add(EFTagType.withPrefixFormatter(path));
        return this;
    }

    public EFTagPrefix prefixOnlyTagPath(String path) {
        tags.add(EFTagType.withPrefixOnlyFormatter(path));
        return this;
    }

    public EFTagPrefix unformattedTagPath(String path) {
        return unformattedTagPath(path, false);
    }

    public EFTagPrefix unformattedTagPath(String path, boolean vanilla) {
        tags.add(EFTagType.withNoFormatter(path, vanilla));
        return this;
    }

    public EFTagPrefix customTagPath(String path,
                                     java.util.function.BiFunction<EFTagPrefix, EFMaterial, TagKey<Item>> formatter) {
        tags.add(EFTagType.withCustomFormatter(path, formatter));
        return this;
    }

    public EFTagPrefix customTagPredicate(String path, boolean vanilla, Predicate<EFMaterial> predicate) {
        tags.add(EFTagType.withCustomFilter(path, vanilla, predicate));
        return this;
    }

    @Unmodifiable
    public List<TagKey<Item>> getItemParentTags() {
        return tags.stream()
                .filter(EFTagType::isParentTag)
                .map(type -> type.getTag(this, EFMaterial.EMPTY))
                .filter(Objects::nonNull)
                .toList();
    }

    @Unmodifiable
    public List<TagKey<Item>> getItemTags(@NotNull EFMaterial material) {
        return tags.stream()
                .filter(type -> !type.isParentTag())
                .map(type -> type.getTag(this, material))
                .filter(Objects::nonNull)
                .toList();
    }

    @Unmodifiable
    public List<TagKey<Item>> getAllItemTags(@NotNull EFMaterial material) {
        return tags.stream()
                .map(type -> type.getTag(this, material))
                .filter(Objects::nonNull)
                .toList();
    }

    @Unmodifiable
    public List<TagKey<Block>> getBlockTags(@NotNull EFMaterial material) {
        return getItemTags(material).stream()
                .map(itemTag -> TagKey.create(Registries.BLOCK, itemTag.location()))
                .toList();
    }

    @Unmodifiable
    public List<TagKey<Block>> getAllBlockTags(@NotNull EFMaterial material) {
        return getAllItemTags(material).stream()
                .map(itemTag -> TagKey.create(Registries.BLOCK, itemTag.location()))
                .toList();
    }

    public String getRegisteredName(@NotNull EFMaterial material) {
        return idPattern.formatted(material.getName());
    }

    public String getUnlocalizedName() {
        return "tagprefix." + getLowerCaseName();
    }

    public String getUnlocalizedName(@NotNull EFMaterial material) {
        String materialKey = "item.%s.%s".formatted(material.getModId(), getRegisteredName(material));
        if (Language.getInstance().has(materialKey)) {
            return materialKey;
        }
        return getUnlocalizedName();
    }

    public MutableComponent getLocalizedName(@NotNull EFMaterial material) {
        return Component.translatable(getUnlocalizedName(material), material.getLocalizedName());
    }

    public String getDefaultEnglishName(@NotNull EFMaterial material) {
        return langValue.formatted(material.getDefaultTranslation());
    }

    public boolean isIgnored(EFMaterial material) {
        return ignoredMaterials.containsKey(material);
    }

    @SafeVarargs
    public final void setIgnored(EFMaterial material, Supplier<? extends ItemLike>... items) {
        setIgnored(material, Arrays.asList(items));
    }

    public void setIgnored(EFMaterial material, Collection<Supplier<? extends ItemLike>> items) {
        ignoredMaterials.computeIfAbsent(material, ignored -> new HashSet<>()).addAll(items);
    }

    public void setIgnored(EFMaterial material, ItemLike... items) {
        List<Supplier<? extends ItemLike>> suppliers = new ArrayList<>();
        for (ItemLike item : items) {
            suppliers.add(() -> item);
        }
        setIgnored(material, suppliers);
    }

    public void setIgnoredBlock(EFMaterial material, Block... blocks) {
        List<Supplier<? extends ItemLike>> suppliers = new ArrayList<>();
        for (Block block : blocks) {
            suppliers.add(() -> block);
        }
        setIgnored(material, suppliers);
    }

    public void setIgnored(EFMaterial material) {
        ignoredMaterials.computeIfAbsent(material, ignored -> new HashSet<>());
    }

    public void removeIgnored(EFMaterial material) {
        ignoredMaterials.remove(material);
    }

    @Unmodifiable
    public Map<EFMaterial, @Unmodifiable Collection<Supplier<? extends ItemLike>>> getIgnored() {
        return Collections.unmodifiableMap(ignoredMaterials);
    }

    public boolean isAmountModified(EFMaterial material) {
        return materialAmounts.containsKey(material);
    }

    public void modifyMaterialAmount(@NotNull EFMaterial material, float amount) {
        materialAmounts.put(material, amount);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof EFTagPrefix prefix && name.equals(prefix.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    private static String toEnglishName(String name) {
        StringBuilder result = new StringBuilder(name.length());
        boolean capitalize = true;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_' || c == '-' || c == '/') {
                result.append(' ');
                capitalize = true;
            } else if (capitalize) {
                result.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
