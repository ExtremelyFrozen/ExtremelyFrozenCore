package com.extfro.extfrocore.common.cover.voiding;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.filter.ItemFilter;
import com.extfro.extfrocore.api.cover.filter.SimpleItemFilter;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.widget.EnumSelectorWidget;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.common.cover.data.VoidingMode;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AdvancedItemVoidingCover extends ItemVoidingCover {

    @SaveField
    @SyncToClient
    @Getter
    private VoidingMode voidingMode = VoidingMode.VOID_ANY;

    @SaveField
    @Getter
    protected int globalVoidingLimit = 1;

    private TextField stackSizeInput;

    public AdvancedItemVoidingCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    //////////////////////////////////////////////
    // *********** COVER LOGIC ***********//
    //////////////////////////////////////////////

    @Override
    protected void doVoidItems() {
        IItemHandler handler = getOwnItemHandler();
        if (handler == null) {
            return;
        }

        switch (voidingMode) {
            case VOID_ANY -> voidAny(handler);
            case VOID_OVERFLOW -> voidOverflow(handler);
        }
    }

    private void voidOverflow(IItemHandler handler) {
        Map<ItemStack, TypeItemInfo> sourceItemAmounts = countInventoryItemsByType(handler);

        for (TypeItemInfo itemInfo : sourceItemAmounts.values()) {
            int itemToVoidAmount = itemInfo.totalCount - getFilteredItemAmount(itemInfo.itemStack);

            if (itemToVoidAmount <= 0) {
                continue;
            }

            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack is = handler.getStackInSlot(slot);
                if (!is.isEmpty() && ItemStack.isSameItemSameComponents(is, itemInfo.itemStack)) {
                    ItemStack extracted = handler.extractItem(slot, itemToVoidAmount, false);

                    if (!extracted.isEmpty()) {
                        itemToVoidAmount -= extracted.getCount();
                    }
                }
                if (itemToVoidAmount == 0) {
                    break;
                }
            }
        }
    }

    private int getFilteredItemAmount(ItemStack itemStack) {
        if (!filterHandler.isFilterPresent())
            return globalVoidingLimit;

        ItemFilter filter = filterHandler.getFilter();
        return filter.isBlackList() ? globalVoidingLimit : filter.testItemCount(itemStack);
    }

    public void setVoidingMode(VoidingMode voidingMode) {
        this.voidingMode = voidingMode;

        configureStackSizeInput();

        if (!this.isRemote()) {
            syncDataHolder.markClientSyncFieldDirty("voidingMode");
            configureFilter();
        }
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    protected @NotNull String getUITitle() {
        return "cover.item.voiding.advanced.title";
    }

    @Override
    protected void buildVoidingAdditionalUI(UIElement group) {
        group.addChild(
                new EnumSelectorWidget<>(146, 20, 20, 20, VoidingMode.values(), voidingMode, this::setVoidingMode));

        this.stackSizeInput = new TextField();
        this.stackSizeInput.layout(layout -> layout.left(64).top(20).width(80).height(20));
        this.stackSizeInput.style(style -> style.background(GuiTextures.DISPLAY));
        this.stackSizeInput.textFieldStyle(style -> style.textColor(0x404040).textShadow(false));
        this.stackSizeInput.setNumbersOnlyInt(1, voidingMode.maxStackSize);
        this.stackSizeInput.setText(String.valueOf(globalVoidingLimit));
        this.stackSizeInput.setTextResponder(value -> {
            if (!value.isBlank()) {
                globalVoidingLimit = Math.max(1, Math.min(voidingMode.maxStackSize, Integer.parseInt(value)));
            }
        });
        configureStackSizeInput();

        group.addChild(this.stackSizeInput);
    }

    @Override
    protected void configureFilter() {
        if (filterHandler.getFilter() instanceof SimpleItemFilter filter) {
            filter.setMaxStackSize(this.voidingMode.maxStackSize);
        }

        configureStackSizeInput();
    }

    private void configureStackSizeInput() {
        if (this.stackSizeInput == null)
            return;

        this.stackSizeInput.setVisible(shouldShowStackSize());
        this.stackSizeInput.setActive(shouldShowStackSize());
        this.stackSizeInput.setNumbersOnlyInt(1, this.voidingMode.maxStackSize);
        this.stackSizeInput.setText(String.valueOf(Math.max(1, Math.min(globalVoidingLimit, this.voidingMode.maxStackSize))));
    }

    private boolean shouldShowStackSize() {
        if (this.voidingMode == VoidingMode.VOID_ANY)
            return false;

        if (!this.filterHandler.isFilterPresent())
            return true;

        return this.filterHandler.getFilter().isBlackList();
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("voidingMode", voidingMode.ordinal());
        tag.putInt("voidSize", globalVoidingLimit);
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setVoidingMode(VoidingMode.values()[tag.getInt("voidingMode")]);
        globalVoidingLimit = tag.getInt("voidSize");
        super.pasteConfig(player, tag);
    }
}
