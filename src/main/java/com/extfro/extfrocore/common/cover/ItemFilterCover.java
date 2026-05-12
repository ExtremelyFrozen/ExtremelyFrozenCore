package com.extfro.extfrocore.common.cover;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.api.cover.filter.ItemFilter;
import com.extfro.extfrocore.api.cover.filter.SmartItemFilter;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.machine.MachineCoverContainer;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.item.ItemHandlerDelegate;
import com.extfro.extfrocore.common.cover.data.FilterMode;
import com.extfro.extfrocore.common.cover.data.ManualIOMode;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemFilterCover extends CoverBehavior implements IUICover {

    protected ItemFilter itemFilter;
    @SaveField
    @SyncToClient
    @Getter
    protected FilterMode filterMode = FilterMode.FILTER_INSERT;
    private FilteredItemHandlerWrapper itemFilterWrapper;
    @SaveField
    @Setter
    @Getter
    protected ManualIOMode allowFlow = ManualIOMode.DISABLED;

    public ItemFilterCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    public ItemFilter getItemFilter() {
        if (itemFilter == null) {
            itemFilter = ItemFilter.loadFilter(attachItem);
            if (itemFilter instanceof SmartItemFilter smart && coverHolder instanceof MachineCoverContainer mcc) {
                var machine = MetaMachine.getMachine(mcc.getLevel(), mcc.getBlockPos());
                if (machine != null) smart.setModeFromMachine(machine.getDefinition().getName());
            }
        }
        return itemFilter;
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        syncDataHolder.markClientSyncFieldDirty("filterMode");
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }

    public ManualIOMode getAllowFlow() {
        return allowFlow;
    }

    public void setAllowFlow(ManualIOMode allowFlow) {
        this.allowFlow = allowFlow;
        syncDataHolder.markClientSyncFieldDirty("allowFlow");
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && coverHolder.getItemHandlerCap(attachedSide, false) != null;
    }

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(IItemHandlerModifiable defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        if (itemFilterWrapper == null || itemFilterWrapper.delegate != defaultValue) {
            this.itemFilterWrapper = new FilteredItemHandlerWrapper(defaultValue);
        }
        return itemFilterWrapper;
    }

    @Override
    public void onAttached(ItemStack itemStack, @Nullable ServerPlayer player) {
        super.onAttached(itemStack, player);
    }

    @Override
    public UIElement createUIElement() {
        UIElement group = new UIElement().layout(layout -> layout.left(0).top(0).width(178).height(85));
        Label title = new Label();
        title.setValue(Component.translatable(attachItem.getDescriptionId()));
        title.layout(layout -> layout.left(60).top(5).width(110).height(10));
        title.textStyle(style -> style.textColor(0x404040).textShadow(false));
        group.addChild(title);
        group.addChild(enumButton(35, 25, filterMode, FilterMode.VALUES, this::setFilterMode));
        group.addChild(enumButton(35, 45, allowFlow, ManualIOMode.VALUES, this::setAllowFlow));
        group.addChild(getItemFilter().openConfigurator(62, 25));
        return group;
    }

    private static <T extends Enum<T> & com.extfro.extfrocore.api.gui.widget.EnumSelectorWidget.SelectableEnum> Button enumButton(
                                                                                                                                  int x, int y, T initialValue, T[] values, java.util.function.Consumer<T> setter) {
        Button button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(18).height(18));
        final int[] selected = { java.util.List.of(values).indexOf(initialValue) };
        java.util.function.Consumer<T> apply = value -> {
            button.buttonStyle(style -> style
                    .baseTexture(new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, value.getIcon()))
                    .hoverTexture(new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, value.getIcon()))
                    .pressedTexture(new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, value.getIcon())));
            button.style(style -> style.tooltips(Component.translatable(value.getTooltip())));
        };
        apply.accept(initialValue);
        button.setOnServerClick(event -> {
            selected[0] = (selected[0] + 1) % values.length;
            T value = values[selected[0]];
            setter.accept(value);
            apply.accept(value);
        });
        return button;
    }

    private class FilteredItemHandlerWrapper extends ItemHandlerDelegate {

        public FilteredItemHandlerWrapper(IItemHandlerModifiable delegate) {
            super(delegate);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (filterMode == FilterMode.FILTER_EXTRACT) {
                if (allowFlow == ManualIOMode.DISABLED) {
                    return stack;
                }
                if (allowFlow == ManualIOMode.UNFILTERED) {
                    return super.insertItem(slot, stack, simulate);
                }
            }
            if (!getItemFilter().test(stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (filterMode == FilterMode.FILTER_INSERT) {
                if (allowFlow == ManualIOMode.DISABLED) {
                    return ItemStack.EMPTY;
                }
                if (allowFlow == ManualIOMode.UNFILTERED) {
                    return super.extractItem(slot, amount, simulate);
                }
            }
            ItemStack result = super.extractItem(slot, amount, true);
            if (result.isEmpty() || !getItemFilter().test(result)) {
                return ItemStack.EMPTY;
            }
            return simulate ? result : super.extractItem(slot, amount, false);
        }
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("manualIO", getAllowFlow().ordinal());
        tag.putInt("filterMode", getFilterMode().ordinal());
        tag.put("filter", attachItem.save(coverHolder.getLevel().registryAccess()));
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setAllowFlow(ManualIOMode.values()[tag.getInt("manualIO")]);
        setFilterMode(FilterMode.values()[tag.getInt("filterMode")]);
        itemFilter = ItemFilter.loadFilter(ItemStack
                .parse(coverHolder.getLevel().registryAccess(), tag.getCompound("filter")).orElse(ItemStack.EMPTY));
        super.pasteConfig(player, tag);
    }
}
