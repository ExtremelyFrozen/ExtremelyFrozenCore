package com.extfro.extfrocore.api.cover.filter;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import com.lowdragmc.lowdraglib2.gui.slot.ItemHandlerSlot;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class FilterUIElements {

    private FilterUIElements() {}

    static UIElement group(int x, int y, int width, int height) {
        return new UIElement().layout(layout -> layout.left(x).top(y).width(width).height(height));
    }

    static Button toggleButton(int x, int y, IGuiTexture icon, BooleanSupplier getter, Consumer<Boolean> setter) {
        Button button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(20).height(20));
        button.buttonStyle(style -> style.baseTexture(new GuiTextureGroup(GuiTextures.BUTTON, icon))
                .hoverTexture(new GuiTextureGroup(GuiTextures.BUTTON, icon))
                .pressedTexture(new GuiTextureGroup(GuiTextures.BUTTON, icon)));
        button.style(style -> style.tooltips(Component.translatable(getter.getAsBoolean() ?
                "gui.gtceu.enabled" : "gui.gtceu.disabled")));
        button.setOnServerClick(event -> {
            setter.accept(!getter.getAsBoolean());
            button.style(style -> style.tooltips(Component.translatable(getter.getAsBoolean() ?
                    "gui.gtceu.enabled" : "gui.gtceu.disabled")));
        });
        return button;
    }

    static ItemSlot phantomItemSlot(int x, int y, Supplier<ItemStack> getter, Consumer<ItemStack> setter,
                                    int maxStackSize) {
        CustomItemStackHandler handler = new CustomItemStackHandler(getter.get().copy()) {

            @Override
            public int getSlotLimit(int slot) {
                return maxStackSize;
            }
        };
        handler.setOnContentsChanged(() -> setter.accept(handler.getStackInSlot(0).copy()));

        ItemHandlerSlot backingSlot = new ItemHandlerSlot(handler, 0)
                .setCanTake(player -> false)
                .setCanPlace(stack -> false);
        ItemSlot slot = new ItemSlot(backingSlot);
        slot.layout(layout -> layout.left(x).top(y).width(18).height(18));
        slot.style(style -> style.background(GuiTextures.SLOT));
        slot.xeiPhantom();
        slot.addServerEventListener("mouseDown", event -> {
            event.stopPropagation();
            Player player = event.currentElement.getModularUI().player;
            ItemStack carried = event.currentElement.getModularUI().getMenu().getCarried();
            ItemStack current = getter.get();
            ItemStack next = clickPhantomItem(current, carried, event.button,
                    event.isShiftDown() ? ClickType.QUICK_MOVE : ClickType.PICKUP, maxStackSize);
            setter.accept(next);
            handler.setStackInSlot(0, next);
            if (player != null) {
                player.containerMenu.broadcastChanges();
            }
        });
        return slot;
    }

    private static ItemStack clickPhantomItem(ItemStack current, ItemStack carried, int mouseButton,
                                              ClickType clickType, int maxStackSize) {
        ItemStack next = current.copy();
        if (mouseButton == 1 && carried.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (carried.isEmpty()) {
            if (!next.isEmpty()) {
                int amount = clickType == ClickType.QUICK_MOVE ? maxStackSize : (mouseButton == 1 ? -1 : 1);
                next.grow(amount);
                next.setCount(Math.max(1, Math.min(maxStackSize, next.getCount())));
            }
            return next;
        }
        next = carried.copy();
        next.setCount(Math.min(maxStackSize, mouseButton == 1 ? 1 : carried.getCount()));
        return next;
    }

    static FluidSlot phantomFluidSlot(int x, int y, Supplier<FluidStack> getter, Consumer<FluidStack> setter,
                                      int maxAmount) {
        FluidTank tank = new FluidTank(maxAmount);
        tank.setFluid(getter.get().copy());
        FluidSlot slot = new FluidSlot().bind(tank, 0);
        slot.layout(layout -> layout.left(x).top(y).width(18).height(18));
        slot.style(style -> style.background(GuiTextures.SLOT));
        slot.setAllowClickFilled(false).setAllowClickDrained(false);
        slot.setCapacity(maxAmount);
        slot.xeiPhantom();
        slot.addServerEventListener("mouseDown", event -> {
            event.stopPropagation();
            ItemStack carried = event.currentElement.getModularUI().getMenu().getCarried();
            FluidStack next = event.button == 1 && carried.isEmpty() ? FluidStack.EMPTY :
                    FluidUtil.getFluidContained(carried)
                            .map(fluid -> fluid.copyWithAmount(Math.min(maxAmount, FluidType.BUCKET_VOLUME)))
                            .orElse(FluidStack.EMPTY);
            setter.accept(next);
            tank.setFluid(next.copy());
            slot.setFluid(next.copy());
        });
        return slot;
    }
}
