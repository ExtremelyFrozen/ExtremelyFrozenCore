package com.extfro.extfrocore.api.recipe.ui;

import com.extfro.extfrocore.api.capability.recipe.FluidRecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.ItemRecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.ProgressTextures;
import com.extfro.extfrocore.api.gui.SteamTexture;
import com.extfro.extfrocore.api.recipe.GTRecipe;
import com.extfro.extfrocore.api.recipe.GTRecipeType;
import com.extfro.extfrocore.api.recipe.RecipeCondition;
import com.extfro.extfrocore.integration.xei.handlers.fluid.CycleFluidEntryHandler;
import com.extfro.extfrocore.integration.xei.handlers.item.CycleItemEntryHandler;
import com.extfro.extfrocore.integration.xei.widgets.GTRecipeElement;

import net.minecraft.nbt.CompoundTag;

import com.google.common.collect.Table;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.math.Size;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.DoubleSupplier;

@SuppressWarnings("UnusedReturnValue")
public class GTRecipeTypeUI {

    @Getter
    @Setter
    private Byte2ObjectMap<IGuiTexture> slotOverlays = new Byte2ObjectArrayMap<>();

    private final GTRecipeType recipeType;

    @Getter
    @Setter
    private ProgressTextures progressBarTexture = ProgressTextures.from(GuiTextures.PROGRESS_BAR_ARROW,
            FillDirection.LEFT_TO_RIGHT);
    @Setter
    private SteamTexture steamProgressBarTexture = null;
    @Setter
    private FillDirection steamMoveType = FillDirection.LEFT_TO_RIGHT;
    @Setter
    @Nullable
    protected BiConsumer<GTRecipe, UIElement> uiBuilder;
    @Setter
    @Getter
    protected int maxTooltips = 3;

    private Size xeiSize;
    @Getter
    private int originalWidth;
    @Getter
    private int originalHeight;

    public GTRecipeTypeUI(@NotNull GTRecipeType recipeType) {
        this.recipeType = recipeType;
    }

    public void reloadCustomUI() {
        this.xeiSize = null;
    }

    public Size getJEISize() {
        Size size = this.xeiSize;
        if (size == null) {
            SlotGroup inputs = addInventorySlotGroup(false, false, false);
            SlotGroup outputs = addInventorySlotGroup(true, false, false);
            int maxWidth = Math.max(inputs.width(), outputs.width());
            this.originalWidth = 2 * maxWidth + 40;
            this.originalHeight = Math.max(inputs.height(), outputs.height());
            this.xeiSize = size = Size.of(Math.max(originalWidth, 150),
                    getPropertyHeightShift() + 5 + originalHeight);
        }
        return size;
    }

    public int getOriginalWidth() {
        getJEISize();
        return originalWidth;
    }

    public int getOriginalHeight() {
        getJEISize();
        return originalHeight;
    }

    public record RecipeHolder(DoubleSupplier progressSupplier,
                               Table<IO, RecipeCapability<?>, Object> storages,
                               CompoundTag data,
                               List<RecipeCondition<?>> conditions,
                               boolean isSteam,
                               boolean isHighPressure) {}

    public UIElement createXEIElement(Table<IO, RecipeCapability<?>, Object> storages,
                                      CompoundTag data,
                                      List<RecipeCondition<?>> conditions) {
        UIElement group = createXEIElement(false, false);
        bindXEIElement(group, storages, data, conditions, false, false);
        return group;
    }

    public UIElement createXEIElement(boolean isSteam, boolean isHighPressure) {
        var inputs = addInventorySlotGroup(false, isSteam, isHighPressure);
        var outputs = addInventorySlotGroup(true, isSteam, isHighPressure);
        int maxWidth = Math.max(inputs.width(), outputs.width());
        int height = Math.max(inputs.height(), outputs.height());
        int width = 2 * maxWidth + 40;
        UIElement group = GTRecipeElement.fixed(0, 0, width, height);

        inputs.element().layout(layout -> layout
                .left((maxWidth - inputs.width()) / 2f)
                .top((height - inputs.height()) / 2f));
        outputs.element().layout(layout -> layout
                .left(maxWidth + 40 + (maxWidth - outputs.width()) / 2f)
                .top((height - outputs.height()) / 2f));
        group.addChild(inputs.element());
        group.addChild(outputs.element());

        ProgressBar progress = GTRecipeElement.progressBar(maxWidth + 10, height / 2 - 10, 20, 20,
                getProgressTextures(isSteam, isHighPressure));
        progress.setId("progress");
        group.addChild(progress);
        return group;
    }

    public void bindXEIElement(UIElement template,
                               Table<IO, RecipeCapability<?>, Object> storages,
                               CompoundTag data,
                               List<RecipeCondition<?>> conditions,
                               boolean isSteam,
                               boolean isHighPressure) {
        for (var capabilityEntry : storages.rowMap().entrySet()) {
            IO io = capabilityEntry.getKey();
            for (var storagesEntry : capabilityEntry.getValue().entrySet()) {
                RecipeCapability<?> cap = storagesEntry.getKey();
                Object storage = storagesEntry.getValue();
                String regex = "^%s_[0-9]+$".formatted(cap.slotName(io));
                if (cap == ItemRecipeCapability.CAP && storage instanceof CycleItemEntryHandler handler) {
                    template.selectRegex(regex, ItemSlot.class).forEach(slot -> {
                        int index = slotIndex(slot.getId());
                        if (index >= 0) {
                            slot.bind(handler, index);
                        }
                    });
                } else if (cap == FluidRecipeCapability.CAP && storage instanceof CycleFluidEntryHandler handler) {
                    template.selectRegex(regex, FluidSlot.class).forEach(slot -> {
                        int index = slotIndex(slot.getId());
                        if (index >= 0) {
                            slot.bind(handler, index);
                            GTRecipeElement.rememberFluidStacks(slot, handler, index);
                        }
                    });
                }
            }
        }
    }

    protected SlotGroup addInventorySlotGroup(boolean isOutputs, boolean isSteam, boolean isHighPressure) {
        int maxCount = 0;
        int totalR = 0;
        Object2IntSortedMap<RecipeCapability<?>> map = new Object2IntAVLTreeMap<>(RecipeCapability.COMPARATOR);
        if (isOutputs) {
            for (var value : recipeType.maxOutputs.object2IntEntrySet()) {
                if (value.getKey().doRenderSlot) {
                    int val = value.getIntValue();
                    maxCount = Math.max(maxCount, Math.min(val, 3));
                    totalR += (val + 2) / 3;
                    map.put(value.getKey(), val);
                }
            }
        } else {
            for (var value : recipeType.maxInputs.object2IntEntrySet()) {
                if (value.getKey().doRenderSlot) {
                    int val = value.getIntValue();
                    maxCount = Math.max(maxCount, Math.min(val, 3));
                    totalR += (val + 2) / 3;
                    map.put(value.getKey(), val);
                }
            }
        }
        int width = maxCount * 18 + 8;
        int height = totalR * 18 + 8;
        UIElement group = GTRecipeElement.fixed(0, 0, width, height);
        int index = 0;
        for (var entry : map.object2IntEntrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            int capCount = entry.getIntValue();
            for (int slotIndex = 0; slotIndex < capCount; slotIndex++) {
                addSlot(group, (index % 3) * 18 + 4, (index / 3) * 18 + 4, slotIndex, capCount,
                        cap, isOutputs, isSteam, isHighPressure);
                index++;
            }
            index += (3 - (index % 3)) % 3;
        }
        return new SlotGroup(group, width, height);
    }

    protected void addSlot(UIElement group, int x, int y, int slotIndex, int count, RecipeCapability<?> capability,
                           boolean isOutputs, boolean isSteam, boolean isHighPressure) {
        IGuiTexture texture = getOverlaysForSlot(isOutputs, capability, slotIndex == count - 1, isSteam,
                isHighPressure);
        String id = capability.slotName(isOutputs ? IO.OUT : IO.IN, slotIndex);
        if (capability == FluidRecipeCapability.CAP) {
            group.addChild(GTRecipeElement.fluidSlot(x, y, texture, id));
        } else if (capability == ItemRecipeCapability.CAP) {
            group.addChild(GTRecipeElement.itemSlot(x, y, texture, id));
        }
    }

    protected IGuiTexture getOverlaysForSlot(boolean isOutput, RecipeCapability<?> capability, boolean isLast,
                                             boolean isSteam, boolean isHighPressure) {
        IGuiTexture base = capability == FluidRecipeCapability.CAP ? GuiTextures.FLUID_SLOT :
                (isSteam ? GuiTextures.SLOT_STEAM.get(isHighPressure) : GuiTextures.SLOT);
        byte overlayKey = (byte) ((isOutput ? 2 : 0) + (capability == FluidRecipeCapability.CAP ? 1 : 0) +
                (isLast ? 4 : 0));
        if (slotOverlays.containsKey(overlayKey)) {
            return new GuiTextureGroup(base, slotOverlays.get(overlayKey));
        }
        return base;
    }

    public int getPropertyHeightShift() {
        int maxPropertyCount = maxTooltips + recipeType.getDataInfos().size() + recipeType.getMinRecipeConditions();
        return maxPropertyCount * 10;
    }

    public void appendXEIUI(GTRecipe recipe, UIElement element) {
        if (uiBuilder != null) {
            uiBuilder.accept(recipe, element);
        }
    }

    public GTRecipeTypeUI setSlotOverlay(boolean isOutput, boolean isFluid, IGuiTexture slotOverlay) {
        return this.setSlotOverlay(isOutput, isFluid, false, slotOverlay)
                .setSlotOverlay(isOutput, isFluid, true, slotOverlay);
    }

    public GTRecipeTypeUI setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast, IGuiTexture slotOverlay) {
        this.slotOverlays.put((byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0)), slotOverlay);
        return this;
    }

    public GTRecipeTypeUI setProgressBar(SpriteTexture progressBar, FillDirection moveType) {
        this.progressBarTexture = ProgressTextures.from(progressBar, moveType);
        return this;
    }

    private ProgressTextures getProgressTextures(boolean isSteam, boolean isHighPressure) {
        if (isSteam && steamProgressBarTexture != null) {
            return ProgressTextures.from(steamProgressBarTexture.get(isHighPressure), steamMoveType);
        }
        return progressBarTexture;
    }

    private int slotIndex(String id) {
        int idx = id.lastIndexOf('_');
        if (idx < 0 || idx == id.length() - 1) return -1;
        try {
            return Integer.parseInt(id.substring(idx + 1));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    protected record SlotGroup(UIElement element, int width, int height) {}
}
