package com.extfro.extfrocore.api.gui.widget;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.common.item.behavior.IntCircuitBehaviour;
import com.extfro.extfrocore.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.TaffyPosition;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Used for setting a "ghost" IC for a machine
 */
public class GhostCircuitSlotWidget extends SlotWidget {

    private static final int NO_CONFIG = -1;

    @Getter
    private IItemHandlerModifiable circuitInventory;
    @Nullable
    private UIElement configurator;

    public GhostCircuitSlotWidget() {
        super();
        addServerEventListener(UIEvents.MOUSE_DOWN, this::onCircuitMouseDown);
        addServerEventListener(UIEvents.MOUSE_WHEEL, this::onCircuitMouseWheel);
    }

    public void setCircuitInventory(IItemHandlerModifiable circuitInventory) {
        this.circuitInventory = circuitInventory;
        setHandlerSlot(circuitInventory, 0);
    }

    public boolean isConfiguratorOpen() {
        return configurator != null;
    }

    private void onCircuitMouseDown(UIEvent event) {
        if (!isMouseOverElement(event.x, event.y) || circuitInventory == null) {
            return;
        }
        if (event.button == 0 && event.isShiftDown()) {
            // open popup on shift-left-click
            toggleConfigurator();
        } else if (event.button == 0) {
            // increment on left-click
            setCircuitValue(getNextValue(true));
        } else if (event.button == 1 && event.isShiftDown()) {
            // clear on shift-right-click
            setCircuitValue(NO_CONFIG);
        } else if (event.button == 1) {
            // decrement on right-click
            setCircuitValue(getNextValue(false));
        }
        event.stopPropagation();
    }

    private void onCircuitMouseWheel(UIEvent event) {
        if (isConfiguratorOpen() || !isMouseOverElement(event.x, event.y) || circuitInventory == null) {
            return;
        }
        setCircuitValue(getNextValue(event.deltaY >= 0));
        event.stopPropagation();
    }

    private int getNextValue(boolean increment) {
        int currentValue = IntCircuitBehaviour.getCircuitConfiguration(this.circuitInventory.getStackInSlot(0));
        if (increment) {
            // if at max, loop around to no circuit
            if (currentValue == IntCircuitBehaviour.CIRCUIT_MAX) {
                return 0;
            }
            // if at no circuit, skip 0 and return 1
            if (this.circuitInventory.getStackInSlot(0).isEmpty()) {
                return 1;
            }
            // normal case: increment by 1
            return currentValue + 1;
        } else {
            // if at no circuit, loop around to max
            if (this.circuitInventory.getStackInSlot(0).isEmpty()) {
                return IntCircuitBehaviour.CIRCUIT_MAX;
            }
            // if at 1, skip 0 and return no circuit
            if (currentValue == 1) {
                return NO_CONFIG;
            }
            // normal case: decrement by 1
            return currentValue - 1;
        }
    }

    @Override
    public boolean canMergeSlot(ItemStack stack) {
        return false;
    }

    public void setCircuitValue(int newValue) {
        if (newValue == NO_CONFIG) {
            this.circuitInventory.setStackInSlot(0, ItemStack.EMPTY);
        } else {
            this.circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
        }
    }

    private void toggleConfigurator() {
        if (isConfiguratorOpen()) {
            configurator.removeSelf();
            configurator = null;
            return;
        }
        var mui = getModularUI();
        if (mui != null) {
            configurator = createConfigurator();
            mui.ui.rootElement.addChild(configurator);
        }
    }

    private UIElement positionedElement(int x, int y, int width, int height) {
        return new UIElement().layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(width).height(height));
    }

    public UIElement createConfigurator() {
        var group = positionedElement(0, 0, 174, 132).style(style -> style.backgroundTexture(GuiTextures.BACKGROUND));
        group.addChild(new Label()
                .setText(Component.literal("Programmed Circuit Configuration"))
                .layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE).left(9).top(8).width(156).height(9)));
        group.addChild(new SlotWidget(this.circuitInventory, 0, (174 - 18) / 2, 20,
                !ConfigHolder.INSTANCE.machines.ghostCircuit, !ConfigHolder.INSTANCE.machines.ghostCircuit)
                .setBackground(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY)));
        if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
            group.addChild(button((174 - 18) / 2, 20, 18, 18, IGuiTexture.EMPTY,
                    event -> circuitInventory.setStackInSlot(0, ItemStack.EMPTY)));
        }
        int idx = 0;
        for (int x = 0; x <= 2; x++) {
            for (int y = 0; y <= 8; y++) {
                int finalIdx = idx;
                group.addChild(button(5 + (18 * y), 48 + (18 * x), 18, 18,
                        new GuiTextureGroup(GuiTextures.SLOT,
                                new ItemStackTexture(IntCircuitBehaviour.stack(finalIdx)).scale(16f / 18)),
                        event -> setConfiguratorCircuitValue(finalIdx)));
                idx++;
            }
        }
        for (int x = 0; x <= 5; x++) {
            int finalIdx = x + 27;
            group.addChild(button(5 + (18 * x), 102, 18, 18,
                    new GuiTextureGroup(GuiTextures.SLOT,
                            new ItemStackTexture(IntCircuitBehaviour.stack(finalIdx)).scale(16f / 18)),
                    event -> setConfiguratorCircuitValue(finalIdx)));
        }
        return group;
    }

    private Button button(int x, int y, int width, int height, IGuiTexture texture,
                          java.util.function.Consumer<UIEvent> onClick) {
        var button = new Button().noText();
        button.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(width).height(height));
        button.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
        button.style(style -> style.backgroundTexture(texture));
        button.setOnServerClick(event -> {
            onClick.accept(event);
            event.stopPropagation();
        });
        return button;
    }

    private void setConfiguratorCircuitValue(int value) {
        ItemStack stack = circuitInventory.getStackInSlot(0).copy();
        if (IntCircuitBehaviour.isIntegratedCircuit(stack)) {
            IntCircuitBehaviour.setCircuitConfiguration(stack, value);
            circuitInventory.setStackInSlot(0, stack);
        } else if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
            circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(value));
        }
    }
}
