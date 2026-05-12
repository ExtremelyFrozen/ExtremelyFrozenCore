package com.extfro.extfrocore.integration.ae2.gui.widget;

import com.extfro.extfrocore.integration.ae2.slot.IConfigurableSlot;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.stacks.GenericStack;
import com.lowdragmc.lowdraglib2.gui.sync.SyncValue;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import dev.vfyjxf.taffy.style.TaffyPosition;
import lombok.Getter;

public abstract class ConfigWidget extends UIElement {

    protected final IConfigurableSlot[] config;
    protected IConfigurableSlot[] displayList;
    public final SyncValue<ListTag> slotSync;
    protected final AmountEditor amountEditor;
    protected RPCEmitter setAmountRPC;

    @Getter
    protected final boolean isStocking;

    public ConfigWidget(int x, int y, IConfigurableSlot[] config, boolean isStocking) {
        this.isStocking = isStocking;
        this.config = config;
        layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(x)
                .top(y)
                .width(config.length / 2f * 18)
                .height(18 * 4 + 2));
        this.init();
        this.amountEditor = new AmountEditor(this);
        this.amountEditor.setVisible(false);
        addChild(this.amountEditor);

        this.slotSync = new SyncValue<>("ae_config", ListTag.class, new ListTag());
        this.slotSync.setValueProvider(() -> createSnapshot(provider(), this.config));
        this.slotSync.addListener(this::readSnapshot);
        addSyncValue(this.slotSync);
        addEventListener(UIEvents.TICK, event -> {
            if (getModularUI() != null && getModularUI().player != null && !getModularUI().player.level().isClientSide) {
                this.slotSync.markAsChanged();
            }
        });
        this.setAmountRPC = addRPCEvent(RPCEventBuilder.simple(Integer.class, Long.class, this::setAmount));
        addEventListener(UIEvents.MOUSE_DOWN, event -> {
            clearSlotSelection();
            disableAmountClient();
        }, true);
    }

    protected HolderLookup.Provider provider() {
        var mui = getModularUI();
        if (mui != null && mui.player != null) {
            return mui.player.registryAccess();
        }
        return net.minecraft.core.RegistryAccess.EMPTY;
    }

    private ListTag createSnapshot(HolderLookup.Provider provider, IConfigurableSlot[] slots) {
        var tags = new ListTag();
        for (int index = 0; index < slots.length; index++) {
            IConfigurableSlot slot = slots[index];
            var tag = new CompoundTag();
            tag.putInt("index", index);
            if (slot.getConfig() != null) {
                tag.put("config", GenericStack.writeTag(provider, slot.getConfig()));
            }
            if (slot.getStock() != null) {
                tag.put("stock", GenericStack.writeTag(provider, slot.getStock()));
            }
            tags.add(tag);
        }
        return tags;
    }

    protected void readSnapshot(ListTag tags) {
        HolderLookup.Provider provider = provider();
        for (int i = 0; i < tags.size(); i++) {
            CompoundTag tag = tags.getCompound(i);
            int index = tag.getInt("index");
            if (index < 0 || index >= displayList.length) {
                continue;
            }
            IConfigurableSlot slot = displayList[index];
            slot.setConfig(tag.contains("config") ? GenericStack.readTag(provider, tag.getCompound("config")) : null);
            slot.setStock(tag.contains("stock") ? GenericStack.readTag(provider, tag.getCompound("stock")) : null);
        }
    }

    abstract void init();

    public abstract boolean hasStackInConfig(GenericStack stack);

    public abstract boolean isAutoPull();

    public final IConfigurableSlot getConfig(int index) {
        return this.config[index];
    }

    public final IConfigurableSlot getDisplay(int index) {
        return this.displayList[index];
    }

    @OnlyIn(Dist.CLIENT)
    public void enableAmountClient(int slotIndex) {
        this.amountEditor.setSlotIndex(slotIndex);
        this.amountEditor.setVisible(true);
        this.amountEditor.refreshText();
    }

    @OnlyIn(Dist.CLIENT)
    public void disableAmountClient() {
        this.amountEditor.setSlotIndex(-1);
        this.amountEditor.setVisible(false);
    }

    protected void clearSlotSelection() {
        for (UIElement child : getChildren()) {
            if (child instanceof com.extfro.extfrocore.integration.ae2.gui.widget.slot.AEConfigSlotWidget slot) {
                slot.setSelect(false);
            }
        }
    }

    private void setAmount(Integer index, Long amount) {
        if (index == null || amount == null || index < 0 || index >= config.length) {
            return;
        }
        IConfigurableSlot slot = this.config[index];
        if (amount > 0 && slot.getConfig() != null) {
            slot.setConfig(new GenericStack(slot.getConfig().what(), amount));
            this.slotSync.markAsChanged();
        }
    }

    public boolean isStackValidForSlot(GenericStack stack) {
        if (stack == null || stack.amount() < 0) return true;
        if (!isStocking()) return true;
        return !hasStackInConfig(stack);
    }

    public static class AmountEditor extends UIElement {

        private int index = -1;
        private final ConfigWidget parentWidget;
        private final TextField amountText;

        public AmountEditor(ConfigWidget widget) {
            this.parentWidget = widget;
            layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(31)
                    .top(-50)
                    .width(80)
                    .height(30));
            this.amountText = new TextField();
            this.amountText.layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(3)
                    .top(12)
                    .width(65)
                    .height(13));
            this.amountText.setNumbersOnlyLong(0, Integer.MAX_VALUE);
            this.amountText.setTextResponder(this::setNewAmount);
            addChild(this.amountText);
        }

        public void setSlotIndex(int slotIndex) {
            this.index = slotIndex;
        }

        public void refreshText() {
            this.amountText.setText(getAmountStr(), false);
        }

        private String getAmountStr() {
            if (this.index < 0) {
                return "0";
            }
            IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
            if (slot.getConfig() != null) {
                return String.valueOf(slot.getConfig().amount());
            }
            return "0";
        }

        private void setNewAmount(String amount) {
            try {
                long newAmount = Long.parseLong(amount);
                if (newAmount > 0 && this.index >= 0) {
                    parentWidget.setAmountRPC.send(this.index, newAmount);
                }
            } catch (NumberFormatException ignore) {}
        }

        @Override
        public void drawBackgroundAdditional(GUIContext context) {
            super.drawBackgroundAdditional(context);
            int x = Math.round(getPositionX());
            int y = Math.round(getPositionY());
            com.extfro.extfrocore.api.gui.GuiTextures.BACKGROUND.draw(context.graphics,
                    context.mouseX, context.mouseY, x, y, 80, 30);
            com.lowdragmc.lowdraglib2.gui.util.DrawerHelper.drawStringSized(context.graphics, "Amount",
                    x + 3, y + 3, 0x404040, false, 1f, false);
            com.extfro.extfrocore.api.gui.GuiTextures.DISPLAY.draw(context.graphics,
                    context.mouseX, context.mouseY, x + 3, y + 11, 65, 14);
        }
    }
}
