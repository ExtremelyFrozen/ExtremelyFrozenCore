package com.extfro.extfrocore.integration.ae2.gui.widget.slot;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.core.mixins.FluidStackAccessor;
import com.extfro.extfrocore.integration.ae2.gui.AEUIHelper;
import com.extfro.extfrocore.integration.ae2.gui.widget.ConfigWidget;
import com.extfro.extfrocore.integration.ae2.slot.ExportOnlyAEFluidSlot;
import com.extfro.extfrocore.integration.ae2.slot.ExportOnlyAESlot;
import com.extfro.extfrocore.integration.ae2.slot.IConfigurableSlot;
import com.extfro.extfrocore.integration.ae2.utils.AEUtil;
import com.extfro.extfrocore.utils.FormattingUtil;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import appeng.api.stacks.GenericStack;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;

import static com.lowdragmc.lowdraglib2.gui.util.DrawerHelper.drawStringFixedCorner;

public class AEFluidConfigSlotWidget extends AEConfigSlotWidget {

    private final RPCEmitter clearRPC;
    private final RPCEmitter setConfigRPC;
    private final RPCEmitter setAmountRPC;
    private final RPCEmitter pickupRPC;

    public AEFluidConfigSlotWidget(int x, int y, ConfigWidget widget, int index) {
        super(x, y, widget, index);
        this.clearRPC = addRPCEvent(RPCEventBuilder.simple(Integer.class, this::clearConfig));
        this.setConfigRPC = addRPCEvent(RPCEventBuilder.simple(Integer.class, FluidStack.class, this::setConfig));
        this.setAmountRPC = addRPCEvent(RPCEventBuilder.simple(Integer.class, Integer.class, this::setAmount));
        this.pickupRPC = addRPCEvent(RPCEventBuilder.simple(Integer.class, Boolean.class, this::pickupStock));
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
            var fluid = AEUtil.toFluidStack(config);
            if (!fluid.isEmpty()) {
                DrawerHelper.drawFluidForGui(context.graphics, fluid, stackX, stackY, 16, 16, -1);
                if (!parentWidget.isStocking()) {
                    drawStringFixedCorner(context.graphics,
                            FormattingUtil.formatNumberReadable(config.amount(), true, FormattingUtil.DECIMAL_FORMAT_0F, "B"),
                            stackX + 17, stackY + 17, 16777215, true, 0.5f);
                }
            }
        }
        if (stock != null) {
            var fluid = AEUtil.toFluidStack(stock);
            if (!fluid.isEmpty()) {
                DrawerHelper.drawFluidForGui(context.graphics, fluid, stackX, stackY + 18, 16, 16, -1);
                drawStringFixedCorner(context.graphics,
                        FormattingUtil.formatNumberReadable(stock.amount(), true, FormattingUtil.DECIMAL_FORMAT_0F, "B"),
                        stackX + 17, stackY + 18 + 17, 16777215, true, 0.5f);
            }
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
            GuiTextures.CONFIG_ARROW_DARK.draw(context, x, y, 18, 18);
        } else {
            GuiTextures.FLUID_SLOT.draw(context, x, y, 18, 18);
            GuiTextures.CONFIG_ARROW.draw(context, x, y, 18, 18);
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
                ItemStack hold = parentWidget.getModularUI() != null && parentWidget.getModularUI().player != null ?
                        parentWidget.getModularUI().player.containerMenu.getCarried() : ItemStack.EMPTY;
                FluidUtil.getFluidContained(hold).ifPresent(f -> setConfigRPC.send(this.index, f.copy()));
                if (!parentWidget.isStocking()) {
                    this.parentWidget.enableAmountClient(this.index);
                    this.select = true;
                }
            }
            event.stopPropagation();
        } else if (mouseOverStock(event.x, event.y) && event.button == 0) {
            if (!parentWidget.isStocking() && this.parentWidget.getDisplay(this.index).getStock() != null) {
                pickupRPC.send(this.index, event.isShiftDown());
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
        FluidStack fluid = AEUtil.toFluidStack(slot.getConfig());
        long amt = event.isCtrlDown() ?
                (event.deltaY > 0 ? fluid.getAmount() * 2L : fluid.getAmount() / 2L) :
                (event.deltaY > 0 ? fluid.getAmount() + 1L : fluid.getAmount() - 1L);
        if (amt > 0 && amt < Integer.MAX_VALUE + 1L) {
            setAmountRPC.send(this.index, (int) amt);
            event.stopPropagation();
        }
    }

    private void clearConfig(Integer index) {
        IConfigurableSlot slot = this.parentWidget.getConfig(index);
        slot.setConfig(null);
        this.parentWidget.slotSync.markAsChanged();
    }

    private void setConfig(Integer index, FluidStack fluid) {
        var stack = AEUtil.fromFluidStack(fluid);
        if (!isStackValidForSlot(stack)) return;
        this.parentWidget.getConfig(index).setConfig(stack);
        this.parentWidget.slotSync.markAsChanged();
    }

    private void setAmount(Integer index, Integer amt) {
        IConfigurableSlot slot = this.parentWidget.getConfig(index);
        if (slot.getConfig() != null) {
            slot.setConfig(ExportOnlyAESlot.copy(slot.getConfig(), amt));
            this.parentWidget.slotSync.markAsChanged();
        }
    }

    private void pickupStock(Integer index, Boolean isShiftKeyDown) {
        ExportOnlyAEFluidSlot fluidTank = this.parentWidget.getConfig(index) instanceof ExportOnlyAEFluidSlot fluid ? fluid : null;
        if (fluidTank == null || fluidTank.getFluidAmount() <= 0) return;
        Player player = getModularUI() == null ? null : getModularUI().player;
        if (player == null) return;
        ItemStack currentStack = parentWidget.getModularUI() != null && parentWidget.getModularUI().player != null ?
                parentWidget.getModularUI().player.containerMenu.getCarried() : ItemStack.EMPTY;
        var handler = FluidUtil.getFluidHandler(currentStack).orElse(null);
        if (handler == null) return;
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;
        boolean performedFill = false;
        FluidStack initialFluid = fluidTank.getFluid();
        for (int i = 0; i < maxAttempts; i++) {
            FluidActionResult result = FluidUtil.tryFillContainer(currentStack, fluidTank, Integer.MAX_VALUE, null, false);
            if (!result.isSuccess()) break;
            ItemStack remainingStack = FluidUtil.tryFillContainer(currentStack, fluidTank, Integer.MAX_VALUE, null, true).getResult();
            currentStack.shrink(1);
            performedFill = true;
            if (!remainingStack.isEmpty() && !player.addItem(remainingStack)) {
                player.drop(remainingStack, true);
                break;
            }
        }
        if (performedFill) {
            SoundEvent soundevent = initialFluid.getFluid().getFluidType().getSound(initialFluid, SoundActions.BUCKET_FILL);
            if (soundevent != null) {
                player.level().playSound(null, player, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            if (parentWidget.getModularUI() != null && parentWidget.getModularUI().player != null) {
                parentWidget.getModularUI().player.containerMenu.setCarried(currentStack);
            }
            this.parentWidget.slotSync.markAsChanged();
        }
    }

    public void acceptFluid(FluidStack fluidStack) {
        if (((FluidStackAccessor) (Object) fluidStack).getRawFluid() != Fluids.EMPTY && fluidStack.getAmount() <= 0L) {
            fluidStack.setAmount(1000);
        }
        if (!fluidStack.isEmpty()) {
            setConfigRPC.send(this.index, fluidStack.copy());
        }
    }
}
