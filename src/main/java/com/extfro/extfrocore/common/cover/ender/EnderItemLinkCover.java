package com.extfro.extfrocore.common.cover.ender;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.filter.FilterHandler;
import com.extfro.extfrocore.api.cover.filter.FilterHandlers;
import com.extfro.extfrocore.api.cover.filter.ItemFilter;
import com.extfro.extfrocore.api.misc.virtualregistry.EntryTypes;
import com.extfro.extfrocore.api.misc.virtualregistry.VirtualEnderRegistry;
import com.extfro.extfrocore.api.misc.virtualregistry.VirtualEntry;
import com.extfro.extfrocore.api.misc.virtualregistry.entries.VirtualItemStorage;
import com.extfro.extfrocore.api.sync_system.SyncDataHolder;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.utils.GTTransferUtils;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnderItemLinkCover extends AbstractEnderLinkCover<VirtualItemStorage> {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    protected static final int TRANSFER_RATE = 8;

    @SaveField
    @SyncToClient
    protected VirtualItemStorage storage;
    protected int itemsLeftToTransferLastSecond;
    @Getter
    @SaveField
    @SyncToClient
    protected FilterHandler<ItemStack, ItemFilter> filterHandler;

    public EnderItemLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        itemsLeftToTransferLastSecond = TRANSFER_RATE * 20;
        filterHandler = FilterHandlers.item(this);
        if (!isRemote()) setEntry(VirtualEnderRegistry.getInstance().getOrCreateEntry(getOwner(), EntryTypes.ENDER_ITEM,
                getChannelName()));
    }

    @Override
    public boolean canAttach() {
        return true;
    }

    @Override
    protected String identifier() {
        return "EILink#";
    }

    @Override
    protected VirtualItemStorage getEntry() {
        return storage;
    }

    @Override
    protected void setEntry(VirtualEntry entry) {
        storage = (VirtualItemStorage) entry;
        syncDataHolder.markClientSyncFieldDirty("storage");
    }

    @Override
    protected EntryTypes<VirtualItemStorage> getEntryType() {
        return EntryTypes.ENDER_ITEM;
    }

    @Override
    protected void transfer() {
        long timer = coverHolder.getOffsetTimer();
        if (itemsLeftToTransferLastSecond > 0) {
            itemsLeftToTransferLastSecond -= doTransferItems(itemsLeftToTransferLastSecond);
        }
        if (timer % 20 == 0) itemsLeftToTransferLastSecond = TRANSFER_RATE * 20;
    }

    private int doTransferItems(int max) {
        IItemHandler ownHandler = getOwnItemHandler();
        if (ownHandler == null) return 0;
        return switch (io) {
            case IN -> GTTransferUtils.transferItemsFiltered(ownHandler, storage.getHandler(),
                    filterHandler.getFilter(), max);
            case OUT -> GTTransferUtils.transferItemsFiltered(storage.getHandler(), ownHandler,
                    filterHandler.getFilter(), max);
            default -> 0;
        };
    }

    public @Nullable IItemHandler getOwnItemHandler() {
        return coverHolder.getItemHandlerCap(attachedSide, false);
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.put("filter", filterHandler.getFilterItem().save(coverHolder.getLevel().registryAccess()));
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        filterHandler.setFilterItem(
                ItemStack.parseOptional(coverHolder.getLevel().registryAccess(), tag.getCompound("filter")));
        super.pasteConfig(player, tag);
    }

    @Override
    public @NotNull List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        if (!filterHandler.getFilterItem().isEmpty()) {
            list.add(filterHandler.getFilterItem());
        }
        return list;
    }

    @Override
    protected UIElement addVirtualEntryWidget(VirtualEntry entry, int x, int y, int width, int height, boolean canClick) {
        UIElement group = new UIElement().layout(layout -> layout.left(x).top(y).width(width).height(height));
        for (int i = 0; i < ((VirtualItemStorage) entry).getHandler().getSlots(); i++) {
            int left = 8 * i;
            if (canClick) {
                ItemSlot slot = new ItemSlot().bind(((VirtualItemStorage) entry).getHandler(), i);
                slot.layout(layout -> layout.left(left).top(0).width(width).height(height));
                group.addChild(slot);
            } else {
                ItemPreviewElement slot = new ItemPreviewElement(((VirtualItemStorage) entry).getHandler(), i);
                slot.layout(layout -> layout.left(left).top(0).width(width).height(height));
                group.addChild(slot);
            }
        }
        return group;
    }

    private static class ItemPreviewElement extends UIElement {

        private final IItemHandler handler;
        private final int slot;

        private ItemPreviewElement(IItemHandler handler, int slot) {
            this.handler = handler;
            this.slot = slot;
        }

        @Override
        public void drawBackgroundAdditional(GUIContext guiContext) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                float width = getContentWidth();
                float height = getContentHeight();
                guiContext.pose.pushPose();
                guiContext.pose.scale(width / 16f, height / 16f, 1);
                guiContext.pose.translate(getContentX() * 16 / width, getContentY() * 16 / height, -200);
                DrawerHelper.drawItemStack(guiContext.graphics, stack, 0, 0, guiContext.elementColor, null);
                guiContext.pose.popPose();
            }
            super.drawBackgroundAdditional(guiContext);
        }
    }

    @Override
    protected String getUITitle() {
        return "cover.ender_item_link.title";
    }
}
