package com.extfro.extfrocore.common.cover;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.api.cover.filter.FluidFilter;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.fluid.FluidHandlerDelegate;
import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;
import com.extfro.extfrocore.common.cover.data.FilterMode;
import com.extfro.extfrocore.common.cover.data.ManualIOMode;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class FluidFilterCover extends CoverBehavior implements IUICover {

    protected FluidFilter fluidFilter;
    @SaveField
    @SyncToClient
    @Getter
    protected FilterMode filterMode = FilterMode.FILTER_INSERT;
    private FilteredFluidHandlerWrapper fluidFilterWrapper;
    @SaveField
    @Setter
    @Getter
    protected ManualIOMode allowFlow = ManualIOMode.DISABLED;

    public FluidFilterCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
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
        return super.canAttach() && coverHolder.getFluidHandlerCap(attachedSide, false) != null;
    }

    public FluidFilter getFluidFilter() {
        if (fluidFilter == null) {
            fluidFilter = FluidFilter.loadFilter(attachItem);
        }
        return fluidFilter;
    }

    @Override
    public @Nullable IFluidHandlerModifiable getFluidHandlerCap(@Nullable IFluidHandlerModifiable defaultValue) {
        if (defaultValue == null) {
            return null;
        }

        if (fluidFilterWrapper == null || fluidFilterWrapper.delegate != defaultValue) {
            this.fluidFilterWrapper = new FilteredFluidHandlerWrapper(defaultValue);
        }

        return fluidFilterWrapper;
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
        group.addChild(getFluidFilter().openConfigurator(62, 25));
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

    private class FilteredFluidHandlerWrapper extends FluidHandlerDelegate {

        public FilteredFluidHandlerWrapper(IFluidHandlerModifiable delegate) {
            super(delegate);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (filterMode == FilterMode.FILTER_EXTRACT) {
                if (allowFlow == ManualIOMode.DISABLED) {
                    return 0;
                }
                if (allowFlow == ManualIOMode.UNFILTERED) {
                    return super.fill(resource, action);
                }
            }
            if (!getFluidFilter().test(resource)) {
                return 0;
            }
            return super.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (filterMode == FilterMode.FILTER_INSERT) {
                if (allowFlow == ManualIOMode.DISABLED) {
                    return FluidStack.EMPTY;
                }
                if (allowFlow == ManualIOMode.UNFILTERED) {
                    return super.drain(resource, action);
                }
            }
            if (!getFluidFilter().test(resource)) {
                return FluidStack.EMPTY;
            }
            return super.drain(resource, action);
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
        fluidFilter = FluidFilter.loadFilter(
                ItemStack.parseOptional(coverHolder.getLevel().registryAccess(), tag.getCompound("filter")));
        super.pasteConfig(player, tag);
    }
}
