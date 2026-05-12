package com.extfro.extfrocore.api.cover.filter;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.machine.MachineCoverContainer;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.sync_system.ISyncManaged;
import com.extfro.extfrocore.api.sync_system.SyncDataHolder;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;

import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class FilterHandler<T, F extends Filter<T, F>> implements ISyncManaged {

    @Getter
    private final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    private final ISyncManaged container;

    @SaveField
    @SyncToClient
    @Getter
    private ItemStack filterItem = ItemStack.EMPTY;

    private @Nullable F filter;
    private @Nullable CustomItemStackHandler filterSlot;
    private @Nullable UIElement filterGroup;

    private Consumer<F> onFilterLoaded = (filter) -> {};
    private Consumer<F> onFilterRemoved = (filter) -> {};
    private Consumer<F> onFilterUpdated = (filter) -> {};

    public FilterHandler(ISyncManaged container) {
        this.container = container;
    }

    protected abstract F loadFilter(ItemStack filterItem);

    protected abstract F getEmptyFilter();

    protected abstract boolean canInsertFilterItem(ItemStack itemStack);

    //////////////////////////////////
    // ***** PUBLIC API ******//
    //////////////////////////////////

    public UIElement createFilterSlotUI(int xPos, int yPos) {
        ItemSlot slot = new ItemSlot();
        slot.bind(getFilterSlot(), 0);
        slot.layout(layout -> layout.left(xPos).top(yPos).width(18).height(18));
        slot.style(style -> style.background(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY)));
        return slot;
    }

    public UIElement createFilterConfigUI(int xPos, int yPos, int width, int height) {
        this.filterGroup = FilterUIElements.group(xPos, yPos, width, height);
        if (!this.filterItem.isEmpty()) {
            this.filterGroup.addChild(getFilter().openConfigurator(0, 0));
        }

        return this.filterGroup;
    }

    public boolean isFilterPresent() {
        return filter != null || !filterItem.isEmpty();
    }

    public F getFilter() {
        if (this.filter == null) {
            if (this.filterItem.isEmpty()) {
                return getEmptyFilter();
            } else {
                loadFilterFromItem();
            }
        }

        return this.filter;
    }

    public boolean test(T resource) {
        return getFilter().test(resource);
    }

    public FilterHandler<T, F> onFilterLoaded(Consumer<F> onFilterLoaded) {
        this.onFilterLoaded = onFilterLoaded;
        return this;
    }

    public FilterHandler<T, F> onFilterRemoved(Consumer<F> onFilterRemoved) {
        this.onFilterRemoved = onFilterRemoved;
        return this;
    }

    public FilterHandler<T, F> onFilterUpdated(Consumer<F> onFilterUpdated) {
        this.onFilterUpdated = onFilterUpdated;
        return this;
    }

    ///////////////////////////////////////
    // ***** FILTER HANDLING ******//
    ///////////////////////////////////////

    private CustomItemStackHandler getFilterSlot() {
        if (this.filterSlot == null) {
            this.filterSlot = new CustomItemStackHandler(this.filterItem) {

                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }
            };

            this.filterSlot.setFilter(this::canInsertFilterItem);
            this.filterSlot.setOnContentsChanged(this::updateFilter);
        }

        return this.filterSlot;
    }

    public void setFilterItem(ItemStack item) {
        getFilterSlot().setStackInSlot(0, item);
        updateFilter();
    }

    private void updateFilter() {
        var filterContainer = getFilterSlot();

        if (ExtForCore.isClientThread()) {
            if (!filterContainer.getStackInSlot(0).isEmpty() && !this.filterItem.isEmpty()) {
                return;
            }
        }

        this.filterItem = filterContainer.getStackInSlot(0);
        syncDataHolder.markClientSyncFieldDirty("filterItem");

        if (this.filter != null) {
            this.filter = null;
            this.onFilterRemoved.accept(this.filter);
        }

        loadFilterFromItem();
    }

    private void loadFilterFromItem() {
        if (!this.filterItem.isEmpty()) {
            this.filter = loadFilter(this.filterItem);
            filter.setOnUpdated(this.onFilterUpdated);
            if (filter instanceof SmartItemFilter smart &&
                    container instanceof CoverBehavior cover &&
                    cover.coverHolder instanceof MachineCoverContainer mcc) {
                var machine = MetaMachine.getMachine(mcc.getLevel(), mcc.getBlockPos());
                if (machine != null) {
                    smart.setModeFromMachine(machine.getDefinition().getName());
                }
            }
            this.onFilterLoaded.accept(this.filter);
        }
        updateFilterGroupUI();
    }

    private void updateFilterGroupUI() {
        if (this.filterGroup == null)
            return;

        this.filterGroup.clearAllChildren();

        if (!this.filterItem.isEmpty() && this.filter != null) {
            this.filterGroup.addChild(this.filter.openConfigurator(0, 0));
        }
    }

    @Override
    public void markAsChanged() {
        container.markAsChanged();
    }

    @Override
    public void scheduleRenderUpdate() {
        container.scheduleRenderUpdate();
    }
}
