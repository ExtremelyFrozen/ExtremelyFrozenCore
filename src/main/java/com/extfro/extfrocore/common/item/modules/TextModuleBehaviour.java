package com.extfro.extfrocore.common.item.modules;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.item.component.IAddInformation;
import com.extfro.extfrocore.api.item.component.IMonitorModuleItem;
import com.extfro.extfrocore.api.placeholder.MultiLineComponent;
import com.extfro.extfrocore.api.placeholder.PlaceholderContext;
import com.extfro.extfrocore.api.placeholder.PlaceholderHandler;
import com.extfro.extfrocore.client.renderer.monitor.IMonitorRenderer;
import com.extfro.extfrocore.client.renderer.monitor.MonitorTextRenderer;
import com.extfro.extfrocore.common.data.item.GTDataComponents;
import com.extfro.extfrocore.common.item.datacomponents.TextLineList;
import com.extfro.extfrocore.common.machine.multiblock.electric.CentralMonitorMachine;
import com.extfro.extfrocore.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.elements.codeeditor.CodeEditor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TextModuleBehaviour implements IMonitorModuleItem, IAddInformation {

    private void updateText(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        if (!stack.has(GTDataComponents.PLACEHOLDER_UUID)) {
            stack.set(GTDataComponents.PLACEHOLDER_UUID, UUID.randomUUID());
        }
        MultiLineComponent text = PlaceholderHandler.processPlaceholders(
                getPlaceholderText(stack),
                new PlaceholderContext(
                        group.getTargetLevel(machine.getLevel()),
                        group.getTarget(machine.getLevel()),
                        group.getTargetCoverSide(),
                        group.getPlaceholderSlotsHandler(),
                        group.getTargetCover(machine.getLevel()),
                        null,
                        null,
                        stack.get(GTDataComponents.PLACEHOLDER_UUID)));
        TextLineList previous = stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY);
        stack.set(GTDataComponents.TEXT_LINE_LIST, new TextLineList(text.toImmutable(), previous.scale()));
    }

    @Override
    public void tick(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        this.updateText(stack, machine, group);
    }

    @Override
    public IMonitorRenderer getRenderer(ItemStack stack) {
        TextLineList lines = stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY);
        return new MonitorTextRenderer(MultiLineComponent.of(lines.lines()), Math.max(lines.scale(), .0001));
    }

    @Override
    public UIElement createUIWidget(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        UIElement builder = new UIElement().layout(layout -> layout.width(260).height(165));
        CodeEditor editor = new CodeEditor();
        editor.layout(layout -> layout.left(0).top(0).width(120).height(80));
        editor.style(style -> style.background(GuiTextures.DISPLAY));
        editor.textAreaStyle(style -> style.textColor(0x404040).textShadow(false));
        // editor.setLanguage(PlaceholderHandler.LANG_DEFINITION);

        TextField scaleInput = new TextField();
        scaleInput.layout(layout -> layout.left(-50).top(47).width(40).height(10));
        scaleInput.style(style -> style.background(GuiTextures.DISPLAY)
                .tooltips(Component.translatable("gtceu.gui.central_monitor.text_scale")));
        scaleInput.textFieldStyle(style -> style.textColor(0x404040).textShadow(false));
        scaleInput.setNumbersOnlyFloat(.0001f, 1000f);

        Button saveButton = new Button().noText();
        saveButton.layout(layout -> layout.left(-40).top(22).width(20).height(20));
        saveButton.buttonStyle(style -> style.baseTexture(GuiTextures.BUTTON_CHECK)
                .hoverTexture(GuiTextures.BUTTON_CHECK)
                .pressedTexture(GuiTextures.BUTTON_CHECK));
        saveButton.setOnServerClick(click -> {
            List<Component> lines = editor.getLines().stream()
                    .map(Component::literal)
                    .collect(Collectors.toList());
            float scale = 1.0f;
            try {
                scale = Float.parseFloat(scaleInput.getValue());
            } catch (NumberFormatException ignored) {}
            stack.set(GTDataComponents.FORMAT_STRING_LIST, new TextLineList(lines, scale));
        });
        scaleInput.setText(String.valueOf(Mth.clamp(
                stack.getOrDefault(GTDataComponents.FORMAT_STRING_LIST, TextLineList.EMPTY).scale(),
                .0001f, 1000f)));
        List<String> formatStringLines = stack.getOrDefault(GTDataComponents.FORMAT_STRING_LIST, TextLineList.EMPTY)
                .lines()
                .stream()
                .map(Component::getString)
                .toList();
        editor.setLines(formatStringLines);
        builder.addChild(editor);
        builder.addChild(saveButton);
        UIElement placeholderReference = PlaceholderHandler.getPlaceholderHandlerUI("");
        builder.addChild(scaleInput);
        placeholderReference.layout(layout -> layout.left(-100).top(-50));
        builder.addChild(placeholderReference);
        return builder;
    }

    @Override
    public String getType() {
        return "text";
    }

    public MultiLineComponent getText(ItemStack stack) {
        return MultiLineComponent.of(stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY).lines());
    }

    public float getScale(ItemStack stack) {
        return Math.max(stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY).scale(), .0001f);
    }

    public void setScale(ItemStack stack, float scale) {
        TextLineList previous = stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY);
        stack.set(GTDataComponents.TEXT_LINE_LIST, new TextLineList(previous.lines(), scale));
    }

    public void setPlaceholderText(ItemStack stack, String text) {
        List<Component> lines = Arrays.stream(text.split("\n"))
                .map(Component::literal)
                .map(Component.class::cast)
                .toList();
        TextLineList previous = stack.getOrDefault(GTDataComponents.FORMAT_STRING_LIST, TextLineList.EMPTY);
        stack.set(GTDataComponents.FORMAT_STRING_LIST, new TextLineList(lines, previous.scale()));
    }

    public String getPlaceholderText(ItemStack stack) {
        StringBuilder formatStringLines = new StringBuilder();
        List<Component> lines = stack.getOrDefault(GTDataComponents.FORMAT_STRING_LIST, TextLineList.EMPTY).lines();
        for (Component line : lines) {
            formatStringLines.append(line.getString()).append('\n');
        }
        return formatStringLines.toString();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext context,
                                List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        if (isAdvanced.isAdvanced()) {
            tooltipComponents.add(Component.literal("Placeholder text:").withStyle(ChatFormatting.GOLD));
            tooltipComponents
                    .addAll(stack.getOrDefault(GTDataComponents.FORMAT_STRING_LIST, TextLineList.EMPTY).lines());
            tooltipComponents.add(Component.literal("Processed text:").withStyle(ChatFormatting.GOLD));
            tooltipComponents.addAll(getText(stack));
        }
    }
}
