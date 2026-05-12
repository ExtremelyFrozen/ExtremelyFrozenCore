package com.extfro.extfrocore.integration.kjs.builders;

import com.extfro.extfrocore.api.capability.recipe.*;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.SteamTexture;
import com.extfro.extfrocore.api.recipe.GTRecipe;
import com.extfro.extfrocore.api.recipe.GTRecipeType;
import com.extfro.extfrocore.api.sound.SoundEntry;
import com.extfro.extfrocore.common.data.GTRecipeTypes;
import com.extfro.extfrocore.integration.kjs.helpers.GTResourceLocation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Accessors(chain = true)
public class GTRecipeTypeBuilder extends BuilderBase<GTRecipeType> {

    public transient String name, category;
    public transient final Object2IntMap<RecipeCapability<?>> maxInputs;
    public transient final Object2IntMap<RecipeCapability<?>> maxOutputs;
    private SpriteTexture progressBarTexture;
    private FillDirection progressMoveType;
    @Nullable
    private SteamTexture steamProgressBarTexture;
    private FillDirection steamMoveType;
    private transient final Byte2ObjectMap<IGuiTexture> slotOverlays;
    @Setter
    @Nullable
    protected transient SoundEntry sound;
    @Setter
    protected transient boolean hasResearchSlot;
    @Setter
    protected transient int maxTooltips;

    @Setter
    @Nullable
    private transient GTRecipeType smallRecipeMap;
    @Setter
    @Nullable
    private transient Supplier<ItemStack> iconSupplier;
    @Nullable
    @Setter
    protected transient BiConsumer<GTRecipe, UIElement> uiBuilder;

    public GTRecipeTypeBuilder(ResourceLocation i) {
        super(GTResourceLocation.implicitAsGtceu(i));
        name = this.id.getPath();
        category = "custom";
        maxInputs = new Object2IntOpenHashMap<>();
        maxOutputs = new Object2IntOpenHashMap<>();
        progressBarTexture = GuiTextures.PROGRESS_BAR_ARROW;
        progressMoveType = FillDirection.LEFT_TO_RIGHT;
        steamProgressBarTexture = null;
        steamMoveType = FillDirection.LEFT_TO_RIGHT;
        slotOverlays = new Byte2ObjectArrayMap<>();
        this.sound = null;
        this.hasResearchSlot = false;
        this.maxTooltips = 4;
        this.smallRecipeMap = null;
        this.iconSupplier = null;
        this.uiBuilder = null;
    }

    public GTRecipeTypeBuilder category(String category) {
        this.category = category;
        return this;
    }

    public GTRecipeTypeBuilder setMaxIOSize(int maxInputs, int maxOutputs, int maxFluidInputs, int maxFluidOutputs) {
        return setMaxSize(IO.IN, ItemRecipeCapability.CAP, maxInputs)
                .setMaxSize(IO.IN, FluidRecipeCapability.CAP, maxFluidInputs)
                .setMaxSize(IO.OUT, ItemRecipeCapability.CAP, maxOutputs)
                .setMaxSize(IO.OUT, FluidRecipeCapability.CAP, maxFluidOutputs);
    }

    public GTRecipeTypeBuilder setEUIO(IO io) {
        if (io.support(IO.IN)) {
            setMaxSize(IO.IN, EURecipeCapability.CAP, 1);
        }
        if (io.support(IO.OUT)) {
            setMaxSize(IO.OUT, EURecipeCapability.CAP, 1);
        }
        return this;
    }

    public GTRecipeTypeBuilder setMaxSize(IO io, RecipeCapability<?> cap, int max) {
        if (io == IO.IN || io == IO.BOTH) {
            maxInputs.put(cap, max);
        }
        if (io == IO.OUT || io == IO.BOTH) {
            maxOutputs.put(cap, max);
        }
        return this;
    }

    public GTRecipeTypeBuilder setSlotOverlay(boolean isOutput, boolean isFluid, IGuiTexture slotOverlay) {
        return this.setSlotOverlay(isOutput, isFluid, false, slotOverlay).setSlotOverlay(isOutput, isFluid, true,
                slotOverlay);
    }

    public GTRecipeTypeBuilder setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast,
                                              IGuiTexture slotOverlay) {
        this.slotOverlays.put((byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0)), slotOverlay);
        return this;
    }

    public GTRecipeTypeBuilder setProgressBar(SpriteTexture progressBar, FillDirection moveType) {
        this.progressBarTexture = progressBar;
        this.progressMoveType = moveType;
        return this;
    }

    public GTRecipeTypeBuilder setSteamProgressBar(SteamTexture progressBar, FillDirection moveType) {
        this.steamProgressBarTexture = progressBar;
        this.steamMoveType = moveType;
        return this;
    }

    @Override
    public String getTranslationKeyGroup() {
        return GTRecipeType.LANGUAGE_KEY_PATH;
    }

    @Override
    public GTRecipeType createObject() {
        var type = GTRecipeTypes.register(name, category);
        type.maxInputs.putAll(maxInputs);
        type.maxOutputs.putAll(maxOutputs);
        type.getRecipeUI().getSlotOverlays().putAll(slotOverlays);
        type.getRecipeUI().setProgressBar(progressBarTexture, progressMoveType);
        type.getRecipeUI().setSteamProgressBarTexture(steamProgressBarTexture);
        type.getRecipeUI().setSteamMoveType(steamMoveType);
        type.setSound(sound);
        type.setHasResearchSlot(hasResearchSlot);
        type.setMaxTooltips(maxTooltips);
        type.setSmallRecipeMap(smallRecipeMap);
        type.setIconSupplier(iconSupplier);
        type.setUiBuilder(uiBuilder);
        return type;
    }
}
