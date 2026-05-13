package com.extfro.extfrocore.api.machine.fancyconfigurator;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.IFancyConfigurator;
import com.extfro.extfrocore.api.gui.fancy.IFancyCustomMiddleClickAction;
import com.extfro.extfrocore.api.gui.fancy.IFancyCustomMouseWheelAction;
import com.extfro.extfrocore.common.item.behavior.IntCircuitBehaviour;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.data.lang.LangHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CircuitFancyConfigurator implements IFancyConfigurator, IFancyCustomMouseWheelAction,
                                      IFancyCustomMiddleClickAction {

    private static final int SET_TO_ZERO = 2;
    private static final int SET_TO_EMPTY = 3;
    private static final int SET_TO_N = 4;
    private static final int NO_CONFIG = -1;

    final ItemStackHandler circuitSlot;

    public CircuitFancyConfigurator(ItemStackHandler circuitSlot) {
        this.circuitSlot = circuitSlot;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.circuit.title");
    }

    @Override
    public IGuiTexture getIcon() {
        if (IntCircuitBehaviour.isIntegratedCircuit(circuitSlot.getStackInSlot(0))) {
            return new ItemStackTexture(circuitSlot.getStackInSlot(0));
        }
        return new GuiTextureGroup(new ItemStackTexture(IntCircuitBehaviour.stack(0)),
                new ItemStackTexture(Items.BARRIER));
    }

    @Override
    public boolean mouseWheelMove(BiConsumer<Integer, Consumer<RegistryFriendlyByteBuf>> writeClientAction,
                                  double mouseX, double mouseY, double wheelDelta) {
        if (wheelDelta == 0) return false;
        if (!ConfigHolder.INSTANCE.machines.ghostCircuit && circuitSlot.getStackInSlot(0).isEmpty()) return false;
        int nextValue = getNextValue(wheelDelta > 0);
        if (nextValue == NO_CONFIG) {
            if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
                circuitSlot.setStackInSlot(0, ItemStack.EMPTY);
                writeClientAction.accept(SET_TO_EMPTY, buf -> {});
            }
        } else {
            circuitSlot.setStackInSlot(0, IntCircuitBehaviour.stack(nextValue));
            writeClientAction.accept(SET_TO_N, buf -> buf.writeVarInt(nextValue));
        }
        return true;
    }

    @Override
    public void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {
        switch (id) {
            case SET_TO_ZERO -> {
                if (ConfigHolder.INSTANCE.machines.ghostCircuit || !circuitSlot.getStackInSlot(0).isEmpty())
                    circuitSlot.setStackInSlot(0, IntCircuitBehaviour.stack(0));
            }
            case SET_TO_EMPTY -> {
                if (ConfigHolder.INSTANCE.machines.ghostCircuit || circuitSlot.getStackInSlot(0).isEmpty())
                    circuitSlot.setStackInSlot(0, ItemStack.EMPTY);
                else
                    circuitSlot.setStackInSlot(0, IntCircuitBehaviour.stack(0));
            }
            case SET_TO_N -> {
                if (ConfigHolder.INSTANCE.machines.ghostCircuit || !circuitSlot.getStackInSlot(0).isEmpty())
                    circuitSlot.setStackInSlot(0, IntCircuitBehaviour.stack(buffer.readVarInt()));
            }
        }
    }

    @Override
    public void onMiddleClick(BiConsumer<Integer, Consumer<RegistryFriendlyByteBuf>> writeClientAction) {
        if (!ConfigHolder.INSTANCE.machines.ghostCircuit && !circuitSlot.getStackInSlot(0).isEmpty())
            circuitSlot.setStackInSlot(0, IntCircuitBehaviour.stack(0));
        else
            circuitSlot.setStackInSlot(0, ItemStack.EMPTY);
        writeClientAction.accept(SET_TO_EMPTY, buf -> {});
    }

    @Override
    public UIElement createConfigurator() {
        UIElement group = new UIElement().layout(layout -> layout.width(174).height(132));
        Label title = new Label();
        title.setValue(Component.literal("Programmed Circuit Configuration"));
        title.layout(layout -> layout.left(9).top(8).width(150).height(10));
        title.textStyle(style -> style.textColor(0x404040).textShadow(false));
        group.addChild(title);

        ItemSlot slot = new ItemSlot();
        slot.bind(circuitSlot, 0);
        slot.layout(layout -> layout.left((174 - 18) / 2f).top(20).width(18).height(18));
        slot.style(style -> style.background(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY)));
        group.addChild(slot);

        if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
            Button clear = new Button().noText();
            clear.layout(layout -> layout.left((174 - 18) / 2f).top(20).width(18).height(18));
            clear.buttonStyle(style -> style.baseTexture(IGuiTexture.EMPTY)
                    .hoverTexture(IGuiTexture.EMPTY).pressedTexture(IGuiTexture.EMPTY));
            clear.setOnServerClick(event -> circuitSlot.setStackInSlot(0, ItemStack.EMPTY));
            group.addChild(clear);
        }

        int idx = 0;
        for (int x = 0; x <= 2; x++) {
            for (int y = 0; y <= 8; y++) {
                int finalIdx = idx++;
                group.addChild(circuitButton(5 + (18 * y), 48 + (18 * x), finalIdx));
            }
        }
        for (int x = 0; x <= 5; x++) {
            group.addChild(circuitButton(5 + (18 * x), 102, x + 27));
        }
        return group;
    }

    private Button circuitButton(int x, int y, int configuration) {
        Button button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(18).height(18));
        IGuiTexture texture = new GuiTextureGroup(GuiTextures.SLOT,
                new ItemStackTexture(IntCircuitBehaviour.stack(configuration)).scale(16f / 18));
        button.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
        button.setOnServerClick(event -> {
            ItemStack stack = circuitSlot.getStackInSlot(0).copy();
            if (IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                IntCircuitBehaviour.setCircuitConfiguration(stack, configuration);
                circuitSlot.setStackInSlot(0, stack);
            } else if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
                circuitSlot.setStackInSlot(0, IntCircuitBehaviour.stack(configuration));
            }
        });
        return button;
    }

    @Override
    public List<Component> getTooltips() {
        var list = new ArrayList<>(IFancyConfigurator.super.getTooltips());
        list.addAll(Arrays.stream(
                LangHandler.getMultiLang("gtceu.gui.configurator_slot.tooltip").toArray(new MutableComponent[0]))
                .toList());
        return list;
    }

    private int getNextValue(boolean increment) {
        int currentValue = IntCircuitBehaviour.getCircuitConfiguration(circuitSlot.getStackInSlot(0));
        if (increment) {
            if (currentValue == IntCircuitBehaviour.CIRCUIT_MAX) {
                return 0;
            }
            if (this.circuitSlot.getStackInSlot(0).isEmpty()) {
                return 1;
            }
            return currentValue + 1;
        } else {
            if (this.circuitSlot.getStackInSlot(0).isEmpty() ||
                    (currentValue == 0 && !ConfigHolder.INSTANCE.machines.ghostCircuit)) {
                return IntCircuitBehaviour.CIRCUIT_MAX;
            }
            if (currentValue == 1 && ConfigHolder.INSTANCE.machines.ghostCircuit) {
                return -1;
            }
            return currentValue - 1;
        }
    }
}
