package com.extfro.extfrocore.api.capability.recipe;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.recipe.content.Content;
import com.extfro.extfrocore.api.recipe.content.ContentModifier;
import com.extfro.extfrocore.api.recipe.content.IContentSerializer;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.CustomMapIngredient;
import com.extfro.extfrocore.api.registry.EFRegistries;
import com.extfro.extfrocore.utils.codec.DispatchedMapCodec;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class RecipeCapability<T> {

    public static final Codec<RecipeCapability<?>> DIRECT_CODEC = ExtForCore.ExtForCore_ID.comapFlatMap(
            id -> EFRegistries.RECIPE_CAPABILITIES.getHolder(id)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(
                            () -> "Unknown registry key in " + EFRegistries.RECIPE_CAPABILITY_REGISTRY + ": " + id)),
            (Holder.Reference<RecipeCapability<?>> holder) -> holder.key().location())
            .flatComapMap(Holder.Reference::value,
                    capability -> safeReference(EFRegistries.RECIPE_CAPABILITIES.wrapAsHolder(capability)));

    public static final Codec<Map<RecipeCapability<?>, List<Content>>> CODEC = new DispatchedMapCodec<>(
            RecipeCapability.DIRECT_CODEC,
            RecipeCapability::contentCodec);

    public static final Comparator<RecipeCapability<?>> COMPARATOR = Comparator.comparingInt(capability -> capability.sortIndex);

    public final String name;
    public final int color;
    public final boolean doRenderSlot;
    public final int sortIndex;
    public final IContentSerializer<T> serializer;

    protected RecipeCapability(String name, int color, boolean doRenderSlot, int sortIndex,
                               IContentSerializer<T> serializer) {
        this.name = name;
        this.color = color;
        this.doRenderSlot = doRenderSlot;
        this.sortIndex = sortIndex;
        this.serializer = serializer;
    }

    public static Codec<List<Content>> contentCodec(RecipeCapability<?> capability) {
        return Content.codec(capability).listOf();
    }

    public T copyInner(T content) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), EFRegistries.builtinRegistry());
        serializer.toNetwork(buf, content);
        return serializer.fromNetwork(buf);
    }

    public T copyWithModifier(T content, ContentModifier modifier) {
        return copyInner(content);
    }

    @SuppressWarnings("unchecked")
    public final T copyContent(Object content) {
        return copyInner((T) content);
    }

    @SuppressWarnings("unchecked")
    public final T copyContent(Object content, ContentModifier modifier) {
        return copyWithModifier((T) content, modifier);
    }

    public T of(Object object) {
        return serializer.of(object);
    }

    public T fromNbt(Tag tag, HolderLookup.Provider provider) {
        return serializer.fromNbt(tag, provider);
    }

    public Tag toNbt(Object content, HolderLookup.Provider provider) {
        return serializer.toNbt(of(content), provider);
    }

    public String slotName(IO io) {
        return "%s_%s".formatted(name, io.name().toLowerCase(Locale.ROOT));
    }

    public String slotName(IO io, int index) {
        return "%s_%s_%s".formatted(name, io.name().toLowerCase(Locale.ROOT), index);
    }

    public MutableComponent getName() {
        return Component.translatable("recipe.capability.%s.name".formatted(name));
    }

    public MutableComponent getColoredName() {
        return getName().withStyle(style -> style.withColor(color));
    }

    public boolean isRecipeSearchFilter() {
        return false;
    }

    public List<Object> compressIngredients(@Unmodifiable Collection<Object> ingredients) {
        return new ArrayList<>(ingredients);
    }

    public List<AbstractMapIngredient> getDefaultMapIngredient(Object object) {
        return List.of(new CustomMapIngredient(of(object)));
    }

    public int limitMaxParallelByOutput(IRecipeCapabilityHolder holder,
                                        com.extfro.extfrocore.api.recipe.MachineRecipe recipe,
                                        int maxMultiplier,
                                        boolean tick) {
        return Integer.MAX_VALUE;
    }

    public int getMaxParallelByInput(IRecipeCapabilityHolder holder,
                                     com.extfro.extfrocore.api.recipe.MachineRecipe recipe,
                                     int limit,
                                     boolean tick) {
        return Integer.MAX_VALUE;
    }

    public boolean doMatchInRecipe() {
        return true;
    }

    public boolean doAddGuiSlots() {
        return isRecipeSearchFilter();
    }

    public Object2IntMap<T> makeChanceCache() {
        return new Object2IntOpenHashMap<>();
    }

    public boolean shouldBypassDistinct() {
        return true;
    }

    private static DataResult<Holder.Reference<RecipeCapability<?>>> safeReference(Holder<RecipeCapability<?>> value) {
        return value.getDelegate() instanceof Holder.Reference<RecipeCapability<?>> reference ?
                DataResult.success(reference) : DataResult.error(
                        () -> "Unregistered holder in " + EFRegistries.RECIPE_CAPABILITY_REGISTRY + ": " + value);
    }
}
