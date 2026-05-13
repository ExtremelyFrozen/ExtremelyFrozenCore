package com.extfro.extfrocore.common.item.tool.behavior;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.item.datacomponents.AoESymmetrical;
import com.extfro.extfrocore.api.item.tool.behavior.IToolUIBehavior;
import com.extfro.extfrocore.api.item.tool.behavior.ToolBehaviorType;
import com.extfro.extfrocore.common.data.GTToolBehaviors;
import com.extfro.extfrocore.common.data.item.GTDataComponents;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.factory.HeldItemUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static com.extfro.extfrocore.api.item.tool.ToolHelper.*;

public class AOEConfigUIBehavior implements IToolUIBehavior<AOEConfigUIBehavior> {

    public static final AOEConfigUIBehavior INSTANCE = new AOEConfigUIBehavior();
    public static final Codec<AOEConfigUIBehavior> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, AOEConfigUIBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean openUI(@NotNull Player player, @NotNull InteractionHand hand) {
        return player.isShiftKeyDown() && !player.getItemInHand(hand)
                .getOrDefault(GTDataComponents.AOE, AoESymmetrical.ZERO).isZero();
    }

    @Override
    public ModularUI createUI(HeldItemUIMenuType.HeldItemUIHolder holder) {
        ItemStack held = holder.itemStack;
        final AoESymmetrical.Mutable definition = getAoEDefinition(held).toMutable();
        UIElement root = new UIElement()
                .layout(layout -> layout.width(120).height(80))
                .style(style -> style.background(GuiTextures.BACKGROUND));

        root.addChild(label(6, 10, Component.translatable("item.gtceu.tool.aoe.columns")));
        root.addChild(label(49, 10, Component.translatable("item.gtceu.tool.aoe.rows")));
        root.addChild(label(79, 10, Component.translatable("item.gtceu.tool.aoe.layers")));

        Label columns = valueLabel(23, 65, columnSize(held));
        Label rows = valueLabel(58, 65, rowSize(held));
        Label layers = valueLabel(93, 65, layerSize(held));

        root.addChild(button(15, 24, "+", () -> {
            held.set(GTDataComponents.AOE, definition.increaseColumn().toImmutable());
            holder.player.setItemInHand(holder.hand, held);
            columns.setValue(Component.literal(columnSize(held)));
        }));
        root.addChild(button(15, 44, "-", () -> {
            held.set(GTDataComponents.AOE, definition.decreaseColumn().toImmutable());
            holder.player.setItemInHand(holder.hand, held);
            columns.setValue(Component.literal(columnSize(held)));
        }));
        root.addChild(button(50, 24, "+", () -> {
            held.set(GTDataComponents.AOE, definition.increaseRow().toImmutable());
            holder.player.setItemInHand(holder.hand, held);
            rows.setValue(Component.literal(rowSize(held)));
        }));
        root.addChild(button(50, 44, "-", () -> {
            held.set(GTDataComponents.AOE, definition.decreaseRow().toImmutable());
            holder.player.setItemInHand(holder.hand, held);
            rows.setValue(Component.literal(rowSize(held)));
        }));
        root.addChild(button(85, 24, "+", () -> {
            held.set(GTDataComponents.AOE, definition.increaseLayer().toImmutable());
            holder.player.setItemInHand(holder.hand, held);
            layers.setValue(Component.literal(layerSize(held)));
        }));
        root.addChild(button(85, 44, "-", () -> {
            held.set(GTDataComponents.AOE, definition.decreaseLayer().toImmutable());
            holder.player.setItemInHand(holder.hand, held);
            layers.setValue(Component.literal(layerSize(held)));
        }));

        root.addChild(columns);
        root.addChild(rows);
        root.addChild(layers);
        return ModularUI.of(UI.of(root), holder.player);
    }

    private static Label label(int x, int y, Component text) {
        Label label = new Label();
        label.setValue(text);
        label.layout(layout -> layout.left(x).top(y).width(36).height(10));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        return label;
    }

    private static Label valueLabel(int x, int y, String text) {
        Label label = new Label();
        label.setValue(Component.literal(text));
        label.layout(layout -> layout.left(x).top(y).width(18).height(10));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        return label;
    }

    private static Button button(int x, int y, String text, Runnable action) {
        Button button = new Button();
        button.setText(text);
        button.layout(layout -> layout.left(x).top(y).width(20).height(20));
        button.buttonStyle(style -> style.baseTexture(GuiTextures.BUTTON)
                .hoverTexture(GuiTextures.BUTTON)
                .pressedTexture(GuiTextures.BUTTON));
        button.setOnServerClick(event -> action.run());
        return button;
    }

    private static String columnSize(ItemStack held) {
        return Integer.toString(1 + 2 * held.getOrDefault(GTDataComponents.AOE, AoESymmetrical.ZERO).column());
    }

    private static String rowSize(ItemStack held) {
        return Integer.toString(1 + 2 * held.getOrDefault(GTDataComponents.AOE, AoESymmetrical.ZERO).row());
    }

    private static String layerSize(ItemStack held) {
        return Integer.toString(1 + held.getOrDefault(GTDataComponents.AOE, AoESymmetrical.ZERO).layer());
    }

    @Override
    public ToolBehaviorType<AOEConfigUIBehavior> getType() {
        return GTToolBehaviors.AOE_CONFIG_UI;
    }
}
