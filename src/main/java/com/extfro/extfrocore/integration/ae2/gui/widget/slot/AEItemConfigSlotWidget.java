package com.extfro.extfrocore.integration.ae2.gui.widget.slot;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.integration.ae2.gui.AEUIHelper;
import com.extfro.extfrocore.integration.ae2.gui.widget.ConfigWidget;
import com.extfro.extfrocore.integration.ae2.slot.ExportOnlyAESlot;
import com.extfro.extfrocore.integration.ae2.slot.IConfigurableSlot;
import com.extfro.extfrocore.integration.ae2.utils.AEUtil;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.util.TextFormattingUtil;

import static com.lowdragmc.lowdraglib2.gui.util.DrawerHelper.drawItemStack;
import static com.lowdragmc.lowdraglib2.gui.util.DrawerHelper.drawStringFixedCorner;

public class AEItemConfigSlotWidget extends AEConfigSlotWidget {

    private final RPCEmitter clearRPC;
    private final RPCEmitter setConfigRPC;
    private final RPCEmitter setAmountRPC;
    private final RPCEmitter pickupRPC;

    public AEItemConfigSlotWidget(int x, int y, ConfigWidget widget, int index) {
        super(x, y, widget, index);
        this.clearRPC = addRPCEvent(RPCEventBuilder.simple(Integer.class, this::clearConfig));
        this.setConfigRPC = addRPCEvent(RPCEventBuilder.simple(Integer.class, ItemStack.class, this::setConfig));
        this.setAmountRPC = addRPCEvent(RPCEventBuilder.simple(Integer.class, Long.class, this::setAmount));
        this.pickupRPC = addRPCEvent(RPCEventBuilder.simple(Integer.class, this::pickupStock));
        addEventListener(UIEvents.MOUSE_DOWN, this::onMouseDown);
        addEventListener(UIEvents.MOUSE_WHEEL, this::onMouseWheel);
    }

    @Override
    public void drawBackgroundAdditional(GUIContext context) {
        super.drawBackgroundAdditional(context);
        int x = Math.round(getPositionX());
        int y = Math.round(getPositionY());
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        GenericStack config = slot.getConfig();
        GenericStack stock = slot.getStock();
        drawSlots(context, x, y, parentWidget.isAutoPull());
        if (this.select) {
            GuiTextures.SELECT_BOX.draw(context, x, y, 18, 18);
        }
        int stackX = x + 1;
        int stackY = y + 1;
        if (config != null) {
            ItemStack stack = config.what() instanceof AEItemKey key ? new ItemStack(key.getItem()) : ItemStack.EMPTY;
            drawItemStack(context.graphics, stack, stackX, stackY, 0xFFFFFFFF, null);
            if (!parentWidget.isStocking()) {
                drawStringFixedCorner(context.graphics, TextFormattingUtil.formatLongToCompactString(config.amount(), 4),
                        stackX + 17, stackY + 17, 16777215, true, 0.5f);
            }
        }
        if (stock != null) {
            ItemStack stack = stock.what() instanceof AEItemKey key ? new ItemStack(key.getItem()) : ItemStack.EMPTY;
            drawItemStack(context.graphics, stack, stackX, stackY + 18, 0xFFFFFFFF, null);
            drawStringFixedCorner(context.graphics, TextFormattingUtil.formatLongToCompactString(stock.amount(), 4),
                    stackX + 17, stackY + 18 + 17, 16777215, true, 0.5f);
        }
        if (mouseOverConfig(context.mouseX, context.mouseY)) {
            AEUIHelper.drawSelectionOverlay(context.graphics, stackX, stackY, 16, 16);
        } else if (mouseOverStock(context.mouseX, context.mouseY)) {
            AEUIHelper.drawSelectionOverlay(context.graphics, stackX, stackY + 18, 16, 16);
        }
    }

    private void drawSlots(GUIContext context, int x, int y, boolean autoPull) {
        if (autoPull) {
            GuiTextures.SLOT_DARK.draw(context, x, y, 18, 18);
            GuiTextures.CONFIG_ARROW.draw(context, x, y, 18, 18);
        } else {
            GuiTextures.SLOT.draw(context, x, y, 18, 18);
            GuiTextures.CONFIG_ARROW_DARK.draw(context, x, y, 18, 18);
        }
        GuiTextures.SLOT_DARK.draw(context, x, y + 18, 18, 18);
    }

    private void onMouseDown(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event) {
        if (mouseOverConfig(event.x, event.y)) {
            if (parentWidget.isAutoPull()) {
                return;
            }
            if (event.button == 1) {
                clearRPC.send(this.index);
                if (!parentWidget.isStocking()) {
                    this.parentWidget.disableAmountClient();
                }
            } else if (event.button == 0) {
                ItemStack item = parentWidget.getModularUI() != null && parentWidget.getModularUI().player != null ?
                        parentWidget.getModularUI().player.containerMenu.getCarried() : ItemStack.EMPTY;
                if (!item.isEmpty()) {
                    setConfigRPC.send(this.index, item.copy());
                }
                if (!parentWidget.isStocking()) {
                    this.parentWidget.enableAmountClient(this.index);
                    this.select = true;
                }
            }
            event.stopPropagation();
        } else if (mouseOverStock(event.x, event.y) && event.button == 0) {
            if (!parentWidget.isStocking() && this.parentWidget.getDisplay(this.index).getStock() != null) {
                pickupRPC.send(this.index);
            }
            event.stopPropagation();
        }
    }

    private void onMouseWheel(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event) {
        if (parentWidget.isStocking()) return;
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        if (slot.getConfig() == null || event.deltaY == 0 || !mouseOverConfig(event.x, event.y)) {
            return;
        }
        GenericStack stack = slot.getConfig();
        long amt = event.isCtrlDown() ?
                (event.deltaY > 0 ? stack.amount() * 2L : stack.amount() / 2L) :
                (event.deltaY > 0 ? stack.amount() + 1L : stack.amount() - 1L);
        if (amt > 0 && amt < Integer.MAX_VALUE + 1L) {
            setAmountRPC.send(this.index, amt);
            event.stopPropagation();
        }
    }

    private void clearConfig(Integer index) {
        IConfigurableSlot slot = this.parentWidget.getConfig(index);
        slot.setConfig(null);
        this.parentWidget.slotSync.markAsChanged();
    }

    private void setConfig(Integer index, ItemStack item) {
        var stack = GenericStack.fromItemStack(item);
        if (!isStackValidForSlot(stack)) return;
        this.parentWidget.getConfig(index).setConfig(stack);
        this.parentWidget.slotSync.markAsChanged();
    }

    private void setAmount(Integer index, Long amt) {
        IConfigurableSlot slot = this.parentWidget.getConfig(index);
        if (slot.getConfig() != null) {
            slot.setConfig(new GenericStack(slot.getConfig().what(), amt));
            this.parentWidget.slotSync.markAsChanged();
        }
    }

    private void pickupStock(Integer index) {
        IConfigurableSlot slot = this.parentWidget.getConfig(index);
        ItemStack carried = parentWidget.getModularUI() != null && parentWidget.getModularUI().player != null ?
                parentWidget.getModularUI().player.containerMenu.getCarried() : ItemStack.EMPTY;
        if (slot.getStock() != null && carried.isEmpty() && slot.getStock().what() instanceof AEItemKey) {
            ItemStack stack = AEUtil.toItemStack(slot.getStock());
            if (parentWidget.getModularUI() != null && parentWidget.getModularUI().player != null) {
                parentWidget.getModularUI().player.containerMenu.setCarried(stack);
            }
            GenericStack remaining = ExportOnlyAESlot.copy(slot.getStock(),
                    Math.max(0, (slot.getStock().amount() - stack.getCount())));
            slot.setStock(remaining.amount() == 0 ? null : remaining);
            this.parentWidget.slotSync.markAsChanged();
        }
    }
}
