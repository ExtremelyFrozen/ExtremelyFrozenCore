package com.extfro.extfrocore.api.cover.filter;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.sync_system.ISyncManaged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.sync_system.holder.SyncDataHolder;

import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
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
    private @Nullable UIElement filterGroup;

    private Consumer<F> onFilterLoaded = filter -> {};
    private Consumer<F> onFilterRemoved = filter -> {};
    private Consumer<F> onFilterUpdated = filter -> {};

    protected FilterHandler(ISyncManaged container) {
        this.container = container;
    }

    protected abstract F loadFilter(ItemStack filterItem);

    protected abstract F getEmptyFilter();

    protected abstract boolean canInsertFilterItem(ItemStack itemStack);

    public UIElement createFilterConfigUI(int xPos, int yPos, int width, int height) {
        this.filterGroup = new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
        });
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
            }
            loadFilterFromItem();
        }
        return this.filter == null ? getEmptyFilter() : this.filter;
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

    public boolean setFilterItem(ItemStack itemStack) {
        ItemStack next = itemStack.copy();
        if (!next.isEmpty()) {
            if (!canInsertFilterItem(next)) {
                return false;
            }
            next.setCount(1);
        }
        updateFilter(next);
        return true;
    }

    public void clearFilterItem() {
        updateFilter(ItemStack.EMPTY);
    }

    private void updateFilter(ItemStack nextItem) {
        if (ExtForCore.isClientThread() && !nextItem.isEmpty() && !this.filterItem.isEmpty()) {
            return;
        }

        F removed = this.filter;
        this.filter = null;
        this.filterItem = nextItem;
        syncDataHolder.resyncAllFields();

        if (removed != null) {
            this.onFilterRemoved.accept(removed);
        }
        loadFilterFromItem();
        markAsChanged();
    }

    private void loadFilterFromItem() {
        if (!this.filterItem.isEmpty()) {
            this.filter = loadFilter(this.filterItem);
            this.filter.setOnUpdated(filter -> {
                this.onFilterUpdated.accept(filter);
                markAsChanged();
            });
            this.onFilterLoaded.accept(this.filter);
        }
        updateFilterGroupUI();
    }

    private void updateFilterGroupUI() {
        if (this.filterGroup == null) {
            return;
        }

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
