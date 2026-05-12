package com.extfro.extfrocore.api.gui.widget;

import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigNumber;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;

import java.util.function.Predicate;

@LDLRegister(name = "gtm_phantom_item_slot", group = "widget.gtm_container", priority = 50, registry = "ldlib2:ui_element")
public class PhantomSlotWidget extends SlotWidget {

    private boolean clearSlotOnRightClick;

    @Configurable
    @ConfigNumber(range = { 0, 64 })
    private int maxStackSize = 64;

    private Predicate<ItemStack> validator = stack -> true;

    public PhantomSlotWidget() {
        super();
        initPhantom();
    }

    public PhantomSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition, true, true);
        initPhantom();
    }

    public PhantomSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition,
                             Predicate<ItemStack> validator) {
        this(itemHandler, slotIndex, xPosition, yPosition);
        this.validator = validator;
    }

    private void initPhantom() {
        xeiPhantom();
        addServerEventListener(UIEvents.MOUSE_DOWN, this::handlePhantomMouseDown);
    }

    public PhantomSlotWidget setClearSlotOnRightClick(boolean clearSlotOnRightClick) {
        this.clearSlotOnRightClick = clearSlotOnRightClick;
        return this;
    }

    @ConfigSetter(field = "canTakeItems")
    public PhantomSlotWidget setCanTakeItems(boolean v) {
        return this;
    }

    @ConfigSetter(field = "canPutItems")
    public PhantomSlotWidget setCanPutItems(boolean v) {
        return this;
    }

    public PhantomSlotWidget setMaxStackSize(int stackSize) {
        maxStackSize = stackSize;
        return this;
    }

    private void handlePhantomMouseDown(UIEvent event) {
        if (!isMouseOverElement(event.x, event.y)) {
            return;
        }
        var slot = getSlot();
        if (event.button == 1 && clearSlotOnRightClick && !slot.getItem().isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            var mui = getModularUI();
            var stackHeld = mui == null || mui.getMenu() == null ? ItemStack.EMPTY : mui.getMenu().getCarried();
            ClickType clickType = event.isShiftDown() ? ClickType.QUICK_MOVE : ClickType.PICKUP;
            slotClickPhantom(slot, event.button, clickType, stackHeld);
        }
        event.stopPropagation();
    }

    @Override
    public boolean canMergeSlot(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeStack(net.minecraft.world.entity.player.Player player) {
        return false;
    }

    @Override
    public boolean canPutStack(ItemStack stack) {
        return false;
    }

    public ItemStack slotClickPhantom(Slot slot, int mouseButton, ClickType clickTypeIn, ItemStack stackHeld) {
        ItemStack stack = ItemStack.EMPTY;

        ItemStack stackSlot = slot.getItem();
        if (!stackSlot.isEmpty()) {
            stack = stackSlot.copy();
        }

        if (mouseButton == 2) {
            fillPhantomSlot(slot, ItemStack.EMPTY, mouseButton);
        } else if (mouseButton == 0 || mouseButton == 1) {
            if (stackSlot.isEmpty()) {
                if (!stackHeld.isEmpty()) {
                    fillPhantomSlot(slot, stackHeld, mouseButton);
                }
            } else if (stackHeld.isEmpty()) {
                adjustPhantomSlot(slot, mouseButton, clickTypeIn);
            } else {
                if (!areItemsEqual(stackSlot, stackHeld)) {
                    adjustPhantomSlot(slot, mouseButton, clickTypeIn);
                }
                fillPhantomSlot(slot, stackHeld, mouseButton);
            }
        } else if (mouseButton == 5 && !slot.hasItem()) {
            fillPhantomSlot(slot, stackHeld, mouseButton);
        }
        return stack;
    }

    private void adjustPhantomSlot(Slot slot, int mouseButton, ClickType clickTypeIn) {
        ItemStack stackSlot = slot.getItem();
        int stackSize;
        if (clickTypeIn == ClickType.QUICK_MOVE) {
            stackSize = mouseButton == 0 ? (stackSlot.getCount() + 1) / 2 : stackSlot.getCount() * 2;
        } else {
            stackSize = mouseButton == 0 ? stackSlot.getCount() - 1 : stackSlot.getCount() + 1;
        }

        if (stackSize > slot.getMaxStackSize()) {
            stackSize = slot.getMaxStackSize();
        }

        stackSlot.setCount(Math.min(maxStackSize, stackSize));
        slot.set(stackSlot);
    }

    private void fillPhantomSlot(Slot slot, ItemStack stackHeld, int mouseButton) {
        if (stackHeld.isEmpty()) {
            slot.set(ItemStack.EMPTY);
            return;
        }

        int stackSize = mouseButton == 0 ? stackHeld.getCount() : 1;
        if (stackSize > slot.getMaxStackSize()) {
            stackSize = slot.getMaxStackSize();
        }
        ItemStack phantomStack = stackHeld.copy();
        phantomStack.setCount(Math.min(maxStackSize, stackSize));
        if (validator.test(phantomStack)) slot.set(phantomStack);
    }

    public boolean areItemsEqual(ItemStack itemStack1, ItemStack itemStack2) {
        return ItemStack.matches(itemStack1, itemStack2);
    }
}
