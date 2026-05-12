package com.extfro.extfrocore.integration.ae2.gui.widget.list;

import com.extfro.extfrocore.integration.ae2.gui.AEUIHelper;
import com.extfro.extfrocore.integration.ae2.utils.KeyStorage;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.lowdragmc.lowdraglib2.gui.sync.SyncValue;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import dev.vfyjxf.taffy.style.TaffyPosition;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Display-only LDLib2 UI element for {@link KeyStorage}.
 */
public abstract class AEListGridWidget extends ScrollerView {

    protected final KeyStorage list;
    private final int slotAmountY;
    protected final List<GenericStack> displayList = new ArrayList<>();
    private final SyncValue<ListTag> listSync;
    private int rowCount;

    public AEListGridWidget(int x, int y, int slotsY, KeyStorage internalList) {
        this.list = internalList;
        this.slotAmountY = slotsY;
        this.rowCount = slotsY;
        layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(x)
                .top(y)
                .width(18 + 140)
                .height(slotsY * 18));
        scrollerStyle(style -> style.scrollerViewStyle(0));
        viewPort.layout(layout -> layout.paddingAll(0));
        viewContainer.layout(layout -> layout.width(18 + 140).height(slotsY * 18));

        this.listSync = new SyncValue<>("ae_list", ListTag.class, new ListTag());
        this.listSync.setValueProvider(() -> createSnapshot(provider()));
        this.listSync.addListener(this::readSnapshot);
        addSyncValue(this.listSync);
        addEventListener(UIEvents.TICK, event -> {
            if (getModularUI() != null && getModularUI().player != null && !getModularUI().player.level().isClientSide) {
                this.listSync.markAsChanged();
            }
        });
        rebuildRows(slotsY);
    }

    public GenericStack getAt(int index) {
        return index >= 0 && index < displayList.size() ? displayList.get(index) : null;
    }

    private HolderLookup.Provider provider() {
        var mui = getModularUI();
        if (mui != null && mui.player != null) {
            return mui.player.registryAccess();
        }
        return net.minecraft.core.RegistryAccess.EMPTY;
    }

    private ListTag createSnapshot(HolderLookup.Provider provider) {
        var tags = new ListTag();
        if (this.list == null) {
            return tags;
        }
        for (Object2LongMap.Entry<AEKey> entry : this.list.storage.object2LongEntrySet()) {
            if (!acceptKey(entry.getKey())) {
                continue;
            }
            var tag = new CompoundTag();
            tag.put("key", entry.getKey().toTagGeneric(provider));
            tag.putLong("value", entry.getLongValue());
            tags.add(tag);
        }
        return tags;
    }

    private void readSnapshot(ListTag tags) {
        displayList.clear();
        HolderLookup.Provider provider = provider();
        for (int i = 0; i < tags.size(); i++) {
            var tag = tags.getCompound(i);
            AEKey key = AEKey.fromTagGeneric(provider, tag.getCompound("key"));
            if (key != null && acceptKey(key)) {
                displayList.add(new GenericStack(key, tag.getLong("value")));
            }
        }
        rebuildRows(Math.max(this.slotAmountY, this.displayList.size()));
    }

    private void rebuildRows(int rows) {
        if (rows == this.rowCount && viewContainer.getChildren().size() == rows) {
            return;
        }
        this.rowCount = rows;
        viewContainer.clearAllChildren();
        viewContainer.layout(layout -> layout.width(18 + 140).height(rows * 18));
        for (int index = 0; index < rows; index++) {
            viewContainer.addChild(createDisplayElement(index));
        }
    }

    protected abstract boolean acceptKey(AEKey key);

    protected abstract UIElement createDisplayElement(int index);

    public static class Item extends AEListGridWidget {

        public Item(int x, int y, int slotsY, KeyStorage internalList) {
            super(x, y, slotsY, internalList);
        }

        @Override
        protected boolean acceptKey(AEKey key) {
            return key instanceof AEItemKey;
        }

        @Override
        protected UIElement createDisplayElement(int index) {
            return new AEItemDisplayWidget(this, index);
        }
    }

    public static class Fluid extends AEListGridWidget {

        public Fluid(int x, int y, int slotsY, KeyStorage internalList) {
            super(x, y, slotsY, internalList);
        }

        @Override
        protected boolean acceptKey(AEKey key) {
            return key instanceof AEFluidKey;
        }

        @Override
        protected UIElement createDisplayElement(int index) {
            return new AEFluidDisplayWidget(this, index);
        }
    }

    static abstract class DisplayElement extends UIElement {

        protected final AEListGridWidget gridWidget;
        protected final int index;

        protected DisplayElement(AEListGridWidget gridWidget, int index) {
            this.gridWidget = gridWidget;
            this.index = index;
            layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0)
                    .top(index * 18)
                    .width(18 + 140)
                    .height(18));
        }

        protected int x() {
            return Math.round(getPositionX());
        }

        protected int y() {
            return Math.round(getPositionY());
        }

        @Override
        public void drawBackgroundAdditional(GUIContext context) {
            super.drawBackgroundAdditional(context);
            int x = x();
            int y = y();
            drawSlot(context, x, y);
            com.extfro.extfrocore.api.gui.GuiTextures.NUMBER_BACKGROUND.draw(context.graphics,
                    context.mouseX, context.mouseY, x + 18, y, 140, 18);
            GenericStack stack = gridWidget.getAt(index);
            if (stack != null) {
                drawStack(context, stack, x + 1, y + 1);
                DrawerHelper.drawText(context.graphics, String.format("x%,d", stack.amount()),
                        x + 21, y + 6, 1, 0xFFFFFFFF);
            }
            if (isMouseOver(context.mouseX, context.mouseY)) {
                AEUIHelper.drawSelectionOverlay(context.graphics, x + 1, y + 1, 16, 16);
            }
        }

        protected abstract void drawSlot(GUIContext context, int x, int y);

        protected abstract void drawStack(GUIContext context, GenericStack stack, int x, int y);
    }
}
