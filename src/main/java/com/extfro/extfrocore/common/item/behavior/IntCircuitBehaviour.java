package com.extfro.extfrocore.common.item.behavior;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.item.component.IAddInformation;
import com.extfro.extfrocore.api.item.component.IItemUIFactory;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.feature.IHasCircuitSlot;
import com.extfro.extfrocore.api.machine.trait.NotifiableItemStackHandler;
import com.extfro.extfrocore.common.data.GTItems;
import com.extfro.extfrocore.common.data.item.GTDataComponents;
import com.extfro.extfrocore.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.lowdragmc.lowdraglib2.gui.factory.HeldItemUIMenuType;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;

import java.util.List;

public class IntCircuitBehaviour implements IItemUIFactory, IAddInformation {

    public static final int CIRCUIT_MAX = 32;

    public static ItemStack stack(int configuration) {
        var stack = GTItems.PROGRAMMED_CIRCUIT.asStack();
        setCircuitConfiguration(stack, configuration);
        return stack;
    }

    public static void setCircuitConfiguration(HeldItemUIMenuType.HeldItemUIHolder holder, int configuration) {
        setCircuitConfiguration(holder.itemStack, configuration);
        holder.player.setItemInHand(holder.hand, holder.itemStack);
    }

    public static void setCircuitConfiguration(ItemStack itemStack, int configuration) {
        if (configuration < 0 || configuration > CIRCUIT_MAX)
            throw new IllegalArgumentException("Given configuration number is out of range!");
        itemStack.set(GTDataComponents.CIRCUIT_CONFIG, configuration);
    }

    public static int getCircuitConfiguration(ItemStack itemStack) {
        return itemStack.getOrDefault(GTDataComponents.CIRCUIT_CONFIG, 0);
    }

    public static boolean isIntegratedCircuit(ItemStack itemStack) {
        return GTItems.PROGRAMMED_CIRCUIT.isIn(itemStack);
    }

    // deprecated, not needed (for now)
    @Deprecated
    public static void adjustConfiguration(HeldItemUIMenuType.HeldItemUIHolder holder, int amount) {
        adjustConfiguration(holder.itemStack, amount);
        holder.player.setItemInHand(holder.hand, holder.itemStack);
    }

    // deprecated, not needed (for now)
    @Deprecated
    public static void adjustConfiguration(ItemStack stack, int amount) {
        if (!isIntegratedCircuit(stack)) return;
        int configuration = getCircuitConfiguration(stack);
        configuration += amount;
        configuration = Mth.clamp(configuration, 0, CIRCUIT_MAX);
        setCircuitConfiguration(stack, configuration);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        int configuration = getCircuitConfiguration(stack);
        tooltipComponents.add(Component.translatable("metaitem.int_circuit.configuration", configuration));
    }

    @Override
    public ModularUI createUI(HeldItemUIMenuType.HeldItemUIHolder holder) {
        UIElement root = new UIElement()
                .layout(layout -> layout.width(184).height(132))
                .style(style -> style.background(GuiTextures.BACKGROUND));

        Label label = new Label();
        label.setValue(Component.literal("Programmed Circuit Configuration"));
        label.layout(layout -> layout.left(9).top(8).width(166).height(10));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        root.addChild(label);

        ItemSlot previewSlot = new ItemSlot();
        previewSlot.setItem(stack(getCircuitConfiguration(holder.itemStack)));
        previewSlot.layout(layout -> layout.left(82).top(20).width(18).height(18));
        previewSlot.style(style -> style.background(GuiTextures.SLOT));
        root.addChild(previewSlot);

        int idx = 0;
        for (int x = 0; x <= 2; x++) {
            for (int y = 0; y <= 8; y++) {
                int finalIdx = idx;
                root.addChild(circuitButton(10 + (18 * y), 48 + (18 * x), finalIdx, holder, previewSlot));
                idx++;
            }
        }
        for (int x = 0; x <= 5; x++) {
            int finalIdx = x + 27;
            root.addChild(circuitButton(10 + (18 * x), 102, finalIdx, holder, previewSlot));
        }
        return ModularUI.of(UI.of(root), holder.player);
    }

    private static Button circuitButton(int x, int y, int configuration,
                                        HeldItemUIMenuType.HeldItemUIHolder holder,
                                        ItemSlot previewSlot) {
        Button button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(18).height(18));
        button.buttonStyle(style -> style.baseTexture(circuitButtonTexture(configuration))
                .hoverTexture(circuitButtonTexture(configuration))
                .pressedTexture(circuitButtonTexture(configuration)));
        button.setOnServerClick(event -> {
            setCircuitConfiguration(holder, configuration);
            previewSlot.setItem(stack(configuration));
        });
        return button;
    }

    private static GuiTextureGroup circuitButtonTexture(int configuration) {
        return new GuiTextureGroup(GuiTextures.SLOT, new ItemStackTexture(stack(configuration)).scale(16f / 18));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var stack = context.getItemInHand();
        int circuitSetting = getCircuitConfiguration(stack);
        BlockEntity entity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (entity instanceof MetaMachine machine && context.isSecondaryUseActive()) {
            if (machine instanceof IHasCircuitSlot circuitMachine &&
                    circuitMachine.getCircuitInventory().getSlots() > 0) {
                setCircuitConfig(circuitMachine.getCircuitInventory(), circuitSetting);
            }
            if (!ConfigHolder.INSTANCE.machines.ghostCircuit)
                stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return IItemUIFactory.super.useOn(context);
    }

    void setCircuitConfig(NotifiableItemStackHandler circuit, int value) {
        circuit.setStackInSlot(0, IntCircuitBehaviour.stack(value));
    }
}
