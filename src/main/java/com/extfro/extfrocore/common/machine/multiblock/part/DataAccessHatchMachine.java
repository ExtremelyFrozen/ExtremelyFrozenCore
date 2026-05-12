package com.extfro.extfrocore.common.machine.multiblock.part;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.IDataAccessHatch;
import com.extfro.extfrocore.api.capability.IMonitorComponent;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.ItemRecipeCapability;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.widget.SlotWidget;
import com.extfro.extfrocore.api.machine.feature.IDataInfoProvider;
import com.extfro.extfrocore.api.machine.multiblock.MultiblockControllerMachine;
import com.extfro.extfrocore.api.machine.multiblock.part.TieredPartMachine;
import com.extfro.extfrocore.api.machine.trait.NotifiableItemStackHandler;
import com.extfro.extfrocore.api.recipe.GTRecipe;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.common.data.item.GTDataComponents;
import com.extfro.extfrocore.common.item.behavior.PortableScannerBehavior;
import com.extfro.extfrocore.common.machine.gui.MachineUIHelper;
import com.extfro.extfrocore.common.machine.multiblock.electric.research.DataBankMachine;
import com.extfro.extfrocore.common.recipe.condition.ResearchCondition;
import com.extfro.extfrocore.utils.ItemStackHashStrategy;
import com.extfro.extfrocore.utils.ResearchManager;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.IItemHandler;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;

import java.util.*;

public class DataAccessHatchMachine extends TieredPartMachine
                                    implements IDataAccessHatch, IDataInfoProvider, IMonitorComponent {

    private final Set<GTRecipe> recipes;
    @Getter
    private final boolean isCreative;
    @SaveField
    public final NotifiableItemStackHandler importItems;

    public DataAccessHatchMachine(BlockEntityCreationInfo info, int tier, boolean isCreative) {
        super(info, tier);
        this.isCreative = isCreative;
        this.recipes = isCreative ? Collections.emptySet() : new ObjectOpenHashSet<>();
        this.importItems = attachTrait(createImportItemHandler());
    }

    protected NotifiableItemStackHandler createImportItemHandler() {
        if (isCreative) return new NotifiableItemStackHandler(0, IO.BOTH);
        return new NotifiableItemStackHandler(getInventorySize(), IO.BOTH) {

            @Override
            public void onContentsChanged() {
                super.onContentsChanged();
                rebuildData(isFormed() && getControllers().first() instanceof DataBankMachine);
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                boolean isDataBank = isFormed() && getControllers().first() instanceof DataBankMachine;
                if (ResearchManager.isStackDataItem(stack, isDataBank) && stack.has(GTDataComponents.RESEARCH_ITEM)) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }
        };
    }

    @Override
    public UIElement createUIWidget() {
        int rowSize = (int) Math.sqrt(getInventorySize());
        int xOffset = 18 * rowSize / 2;
        UIElement group = MachineUIHelper.group(18 * rowSize, 18 * rowSize);

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                group.addChild(new SlotWidget(importItems, index,
                        rowSize * 9 + x * 18 - xOffset, y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        return group;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return !this.isCreative;
    }

    protected int getInventorySize() {
        return switch (getTier()) {
            case EFValues.LuV -> 16;
            case EFValues.EV -> 9;
            case EFValues.HV -> 4;
            default -> 1;
        };
    }

    private void rebuildData(boolean isDataBank) {
        if (isCreative || getLevel() == null || getLevel().isClientSide) return;
        recipes.clear();
        for (int i = 0; i < this.importItems.getSlots(); i++) {
            ItemStack stack = this.importItems.getStackInSlot(i);
            ResearchManager.ResearchItem researchData = stack.get(GTDataComponents.RESEARCH_ITEM);
            boolean isValid = ResearchManager.isStackDataItem(stack, isDataBank);
            if (researchData != null && isValid) {
                Collection<GTRecipe> collection = researchData.recipeType()
                        .getDataStickEntry(researchData.researchId());
                if (collection != null) {
                    recipes.addAll(collection);
                }
            }
        }
    }

    @Override
    public boolean isRecipeAvailable(GTRecipe recipe, Collection<IDataAccessHatch> seen) {
        seen.add(this);
        return recipe.conditions.stream().noneMatch(ResearchCondition.class::isInstance) || recipes.contains(recipe);
    }

    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_RECIPE_INFO) {
            if (recipes.isEmpty())
                return Collections.emptyList();
            List<Component> list = new ArrayList<>();

            list.add(Component.translatable("behavior.data_item.title"));
            list.add(Component.empty());
            Collection<ItemStack> itemsAdded = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingAll());
            for (GTRecipe recipe : recipes) {
                ItemStack stack = ItemRecipeCapability.CAP
                        .of(recipe.getOutputContents(ItemRecipeCapability.CAP).getFirst().content).getItems()[0];
                if (!itemsAdded.contains(stack)) {
                    itemsAdded.add(stack);
                    list.add(Component.translatable("behavior.data_item.data", stack.getDisplayName()));
                }
            }
            return list;
        }
        return new ArrayList<>();
    }

    @Override
    public boolean canShared() {
        return isCreative;
    }

    @Override
    public void addedToController(MultiblockControllerMachine controller) {
        rebuildData(controller instanceof DataBankMachine);
        super.addedToController(controller);
    }

    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        return IDataAccessHatch.super.modifyRecipe(recipe);
    }

    @Override
    public IGuiTexture getComponentIcon() {
        return SpriteTexture.of(ExtForCore.id("textures/item/data_module.png").toString()).getSubTexture(0, 0, 1, 1 / 13f);
    }

    @Override
    public IItemHandler getDataItems() {
        return importItems.storage;
    }
}
