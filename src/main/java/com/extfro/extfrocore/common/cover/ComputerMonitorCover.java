package com.extfro.extfrocore.common.cover;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.machine.TickableSubscription;
import com.extfro.extfrocore.api.machine.feature.IDataStickInteractable;
import com.extfro.extfrocore.api.placeholder.IPlaceholderInfoProviderCover;
import com.extfro.extfrocore.api.placeholder.MultiLineComponent;
import com.extfro.extfrocore.api.placeholder.PlaceholderContext;
import com.extfro.extfrocore.api.placeholder.PlaceholderHandler;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;
import com.extfro.extfrocore.client.renderer.cover.CoverTextRenderer;
import com.extfro.extfrocore.client.renderer.cover.IDynamicCoverRenderer;
import com.extfro.extfrocore.common.data.item.GTDataComponents;
import com.extfro.extfrocore.common.item.datacomponents.ComputerMonitorConfig;
import com.extfro.extfrocore.data.lang.LangHandler;
import com.extfro.extfrocore.integration.create.GTCreateIntegration;
import com.extfro.extfrocore.utils.GTStringUtils;
import com.extfro.extfrocore.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComputerMonitorCover extends CoverBehavior
                                  implements IUICover, IDataStickInteractable, IPlaceholderInfoProviderCover {

    private TickableSubscription subscription;
    private final CoverTextRenderer renderer;
    @SaveField
    @Getter
    private List<String> formatStringArgs = new ArrayList<>(8);
    @SaveField
    @Getter
    private List<String> formatStringLines = new ArrayList<>(8);
    @SaveField
    @SyncToClient
    @Getter
    private List<MutableComponent> text = new ArrayList<>();
    @SaveField
    public CustomItemStackHandler itemStackHandler = new CustomItemStackHandler(8);
    @Setter
    private String placeholderSearch = "";
    @Setter
    @Getter
    @SaveField
    private int updateInterval = 100;
    @Getter
    @SaveField
    private long ticksSincePlaced = 0;
    @SaveField
    @Getter
    private List<MutableComponent> createDisplayTargetBuffer = new ArrayList<>();
    @SaveField
    @Getter
    private List<MutableComponent> computerCraftTextBuffer = new ArrayList<>();
    @SaveField
    @Getter
    private UUID placeholderUUID;

    public ComputerMonitorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        renderer = new CoverTextRenderer(this::getText);
        placeholderUUID = UUID.randomUUID();
        for (int i = 0; i < 100; i++) {
            createDisplayTargetBuffer.add(Component.empty());
            computerCraftTextBuffer.add(Component.empty());
        }
    }

    public List<MutableComponent> getRenderedText() {
        String s = formatStringLines.stream().reduce((a, b) -> a + "\n" + b).orElse("");
        List<String> tmp = new ArrayList<>(formatStringArgs);
        tmp = tmp.stream().map(str -> '{' + str + '}').toList();
        return PlaceholderHandler.processPlaceholders(
                GTStringUtils.replace(s, "\\{}", tmp),
                new PlaceholderContext(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide,
                        itemStackHandler,
                        this, null, new MultiLineComponent(text), placeholderUUID));
    }

    public void setDisplayTargetBufferLine(int line, MutableComponent component) {
        createDisplayTargetBuffer.set(line, component);
    }

    @Override
    public void setComputerCraftTextBufferLine(int line, MutableComponent component) {
        computerCraftTextBuffer.set(line, component);
    }

    @Override
    public boolean canPipePassThrough() {
        return false;
    }

    @Override
    public Supplier<IDynamicCoverRenderer> getDynamicRenderer() {
        return () -> renderer;
    }

    @Override
    public UIElement createUIElement() {
        int textFieldWidth = 160, horizontalPadding = 10, verticalPadding = 2;
        final UIElement group = new UIElement()
                .layout(layout -> layout.width(2 * textFieldWidth + 3 * horizontalPadding).height(150));
        final UIElement mainPage = new UIElement()
                .layout(layout -> layout.width(2 * textFieldWidth + 3 * horizontalPadding).height(150));
        final UIElement formatStringArgsPage = new UIElement()
                .layout(layout -> layout.width(2 * textFieldWidth + 3 * horizontalPadding).height(150));
        for (int i = 0; i < 8; i++) {
            int finalI = i;
            if (i >= formatStringLines.size()) formatStringLines.add("");
            TextField formatStringInput = textField(
                    horizontalPadding + textFieldWidth / 2,
                    10 + verticalPadding + i * (15 + verticalPadding),
                    textFieldWidth, 15, formatStringLines.get(i),
                    s -> formatStringLines.set(finalI, s));
            formatStringInput.style(style -> style.tooltips(LangHandler
                    .getMultiLang("gtceu.gui.computer_monitor_cover.main_textbox_tooltip", i + 1)
                    .toArray(Component[]::new)));
            mainPage.addChild(formatStringInput);

            ItemSlot slot = new ItemSlot().bind(itemStackHandler, i);
            slot.layout(layout -> layout.left(horizontalPadding + 50).top(20 * finalI).width(18).height(18));
            slot.style(style -> style.background(GuiTextures.SLOT).tooltips(LangHandler
                    .getMultiLang("gtceu.gui.computer_monitor_cover.slot_tooltip", i + 1)
                    .toArray(Component[]::new)));
            mainPage.addChild(slot);
        }
        for (int i = 0; i < 8; i++) {
            int finalI = i;
            if (i >= formatStringArgs.size()) formatStringArgs.add("");
            TextField formatStringArgsInput = textField(
                    textFieldWidth / 2 + horizontalPadding,
                    10 + verticalPadding + i * (15 + verticalPadding),
                    textFieldWidth, 15, formatStringArgs.get(i),
                    s -> formatStringArgs.set(finalI, s));
            formatStringArgsInput.style(style -> style.tooltips(LangHandler
                    .getMultiLang("gtceu.gui.computer_monitor_cover.second_page_textbox_tooltip",
                            GTStringUtils.getIntOrderingSuffix(i + 1))
                    .toArray(Component[]::new)));
            formatStringArgsPage.addChild(formatStringArgsInput);
        }
        Button switchToFormatStringArgsPageButton = button(
                horizontalPadding + 50,
                10 * (15 + verticalPadding) + verticalPadding,
                20, 20, GuiTextures.BUTTON_RIGHT);
        switchToFormatStringArgsPageButton.setOnClick(event -> {
            group.clearAllChildren();
            group.addChild(formatStringArgsPage);
        });
        Button switchBack = button(
                horizontalPadding + 50,
                10 * (15 + verticalPadding) + verticalPadding,
                20, 20, GuiTextures.BUTTON_LEFT);
        switchBack.setOnClick(event -> {
            group.clearAllChildren();
            group.addChild(mainPage);
        });
        UIElement placeholderReference = PlaceholderHandler.getPlaceholderHandlerUI("");
        placeholderReference.layout(layout -> layout.left(280).top(0).width(160).height(215));
        mainPage.addChild(placeholderReference);
        TextField updateIntervalInput = intInput(0, 0, 60, 20, updateInterval, 1, 60 * 20, this::setUpdateInterval);
        updateIntervalInput.style(style -> style.tooltips(
                Component.translatable("gtceu.gui.computer_monitor_cover.update_interval")));
        mainPage.addChild(updateIntervalInput);
        switchToFormatStringArgsPageButton.style(style -> style.tooltips(
                Component.translatable("gtceu.gui.computer_monitor_cover.edit_blank_placeholders")));
        switchBack.style(style -> style.tooltips(
                Component.translatable("gtceu.gui.computer_monitor_cover.edit_displayed_text")));
        mainPage.addChild(switchToFormatStringArgsPageButton);
        formatStringArgsPage.addChild(switchBack);
        group.addChild(mainPage);
        return group;
    }

    private TextField textField(int x, int y, int width, int height, String value,
                                java.util.function.Consumer<String> responder) {
        TextField field = new TextField();
        field.layout(layout -> layout.left(x).top(y).width(width).height(height));
        field.style(style -> style.background(GuiTextures.DISPLAY));
        field.textFieldStyle(style -> style.textColor(0x404040).textShadow(false));
        field.setAnyString();
        field.setText(value);
        field.setTextResponder(responder);
        return field;
    }

    private TextField intInput(int x, int y, int width, int height, int value, int min, int max,
                               java.util.function.Consumer<Integer> responder) {
        TextField field = textField(x, y, width, height, String.valueOf(value), text -> {
            if (!text.isBlank()) {
                responder.accept(Integer.parseInt(text));
            }
        });
        field.setNumbersOnlyInt(min, max);
        return field;
    }

    private Button button(int x, int y, int width, int height, IGuiTexture icon) {
        Button button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(width).height(height));
        IGuiTexture texture = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, icon);
        button.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
        return button;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscription = coverHolder.subscribeServerTick(subscription, this::update);
    }

    private void update() {
        ticksSincePlaced++;
        if (coverHolder.getOffsetTimer() % updateInterval == 0) {
            try {
                if (ExtForCore.Mods.isCreateLoaded())
                    GTCreateIntegration.TemporaryRedstoneLinkTransmitter.destroyAll();
                setRedstoneSignalOutput(0);
                text = getRenderedText();
            } catch (RuntimeException e) {
                text = GTUtil.list(
                        Component.translatable("gtceu.computer_monitor_cover.error.exception", e.getMessage()));
            }
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        List<ItemStack> drops = super.getAdditionalDrops();
        for (int i = 0; i < 8; i++) {
            if (!itemStackHandler.getStackInSlot(i).isEmpty()) {
                drops.add(itemStackHandler.getStackInSlot(i));
            }
        }
        return drops;
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        ComputerMonitorConfig config = dataStick.get(GTDataComponents.COMPUTER_MONITOR_CONFIG);
        if (config == null) return InteractionResult.FAIL;

        formatStringLines.clear();
        formatStringLines.addAll(config.lines());

        formatStringArgs.clear();
        formatStringArgs.addAll(config.args());
        updateInterval = config.updateInterval();
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        dataStick.set(GTDataComponents.COMPUTER_MONITOR_CONFIG,
                new ComputerMonitorConfig(formatStringLines, formatStringArgs, updateInterval));
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}
