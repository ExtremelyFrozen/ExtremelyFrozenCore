package com.extfro.extfrocore.common.recipe.condition;

import com.extfro.extfrocore.api.data.DimensionMarker;
import com.extfro.extfrocore.api.gui.widget.SlotWidget;
import com.extfro.extfrocore.api.machine.trait.RecipeLogic;
import com.extfro.extfrocore.api.recipe.GTRecipe;
import com.extfro.extfrocore.api.recipe.RecipeCondition;
import com.extfro.extfrocore.api.recipe.condition.RecipeConditionType;
import com.extfro.extfrocore.api.registry.GTRegistries;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;
import com.extfro.extfrocore.common.data.GTRecipeConditions;
import com.extfro.extfrocore.config.ConfigHolder;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class DimensionCondition extends RecipeCondition<DimensionCondition> {

    // spotless:off
    public static final MapCodec<DimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> RecipeCondition.isReverse(instance).and(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(DimensionCondition::getDimension)
    ).apply(instance, DimensionCondition::new));
    // spotless:on

    @Getter
    private ResourceKey<Level> dimension;

    public DimensionCondition(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public DimensionCondition(boolean isReverse, ResourceKey<Level> dimension) {
        super(isReverse);
        this.dimension = dimension;
    }

    @Override
    public RecipeConditionType<DimensionCondition> getType() {
        return GTRecipeConditions.DIMENSION;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    @Override
    public Component getTooltips() {
        return Component.translatableEscape("recipe.condition.dimension.tooltip", getDimensionName(this.dimension));
    }

    public SlotWidget setupDimensionMarkers(int xOffset, int yOffset) {
        DimensionMarker dimMarker = GTRegistries.DIMENSION_MARKERS.getOptional(this.dimension.location())
                .orElse(new DimensionMarker(DimensionMarker.MAX_TIER,
                        () -> Blocks.BARRIER, getDimensionName(this.dimension)));
        ItemStack icon = dimMarker.getIcon();
        CustomItemStackHandler handler = new CustomItemStackHandler(1);
        SlotWidget dimSlot = new SlotWidget(handler, 0, xOffset, yOffset, false, false)
                .setIngredientIO(IngredientIO.INPUT);
        handler.setStackInSlot(0, icon);
        if (ConfigHolder.INSTANCE.compat.showDimensionTier) {
            dimSlot.setOverlay(
                    new TextTexture("T" + (dimMarker.tier >= DimensionMarker.MAX_TIER ? "?" : dimMarker.tier))
                            .scale(0.75f).transform(-3.0f, 5.0f));
        }
        return dimSlot;
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.machine.self().getLevel();
        return level != null && dimension == level.dimension();
    }

    @Override
    public DimensionCondition createTemplate() {
        return new DimensionCondition();
    }

    public static Component getDimensionName(ResourceKey<Level> dimension) {
        return getDimensionName(dimension.location());
    }

    public static Component getDimensionName(ResourceLocation dimension) {
        return Component.translatableWithFallback(dimension.toLanguageKey(Level.TRANSLATION_PREFIX),
                dimension.toString());
    }
}
