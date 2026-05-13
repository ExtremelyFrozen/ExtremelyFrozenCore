package com.extfro.extfrocore.common.cover.ender;

import com.extfro.extfrocore.api.capability.IControllable;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.api.cover.filter.FilterHandler;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.widget.EnumSelectorWidget;
import com.extfro.extfrocore.api.machine.ConditionalSubscriptionHandler;
import com.extfro.extfrocore.api.machine.MachineCoverContainer;
import com.extfro.extfrocore.api.misc.virtualregistry.EntryTypes;
import com.extfro.extfrocore.api.misc.virtualregistry.VirtualEnderRegistry;
import com.extfro.extfrocore.api.misc.virtualregistry.VirtualEntry;
import com.extfro.extfrocore.api.misc.virtualregistry.entries.VirtualTank;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.common.cover.data.ManualIOMode;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

@SuppressWarnings("SameParameterValue")
public abstract class AbstractEnderLinkCover<T extends VirtualEntry> extends CoverBehavior
                                            implements IUICover, IControllable {

    public static final Pattern COLOR_INPUT_PATTERN = Pattern.compile("^[0-9a-fA-F]{0,8}$");

    protected final ConditionalSubscriptionHandler subscriptionHandler;

    @SaveField
    @SyncToClient
    protected String colorStr = VirtualEntry.DEFAULT_COLOR;
    @Getter
    @SaveField
    @SyncToClient
    protected Permissions permission = Permissions.PUBLIC;
    @SaveField
    @Getter
    protected boolean isWorkingEnabled = true;
    @Getter
    @SaveField
    @SyncToClient
    protected ManualIOMode manualIOMode = ManualIOMode.DISABLED;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected IO io = IO.OUT;
    protected VirtualEntryElement virtualEntryElement;
    @SyncToClient
    boolean isAnyChanged = false;

    public AbstractEnderLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isSubscriptionActive);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscriptionHandler.initialize(coverHolder.getLevel());
    }

    @Override
    public abstract boolean canAttach();

    @Override
    public void onAttached(@NotNull ItemStack itemStack, @Nullable ServerPlayer player) {
        super.onAttached(itemStack, player);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        subscriptionHandler.unsubscribe();
        if (!isRemote()) {
            VirtualEnderRegistry.getInstance()
                    .deleteEntryIf(getOwner(), getEntryType(), getChannelName(), VirtualEntry::canRemove);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        subscriptionHandler.unsubscribe();
        if (!isRemote()) {
            VirtualEnderRegistry.getInstance()
                    .deleteEntryIf(getOwner(), getEntryType(), getChannelName(), VirtualEntry::canRemove);
        }
    }

    @Override
    public void onUIClosed() {
        virtualEntryElement = null;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.isWorkingEnabled != isWorkingAllowed) {
            this.isWorkingEnabled = isWorkingAllowed;
            subscriptionHandler.updateSubscription();
        }
    }

    @Override
    public @NotNull UIElement createUIElement() {
        virtualEntryElement = new VirtualEntryElement(this);
        return virtualEntryElement;
    }

    public void setIo(IO io) {
        if (io == IO.IN || io == IO.OUT) {
            this.io = io;
            syncDataHolder.markClientSyncFieldDirty("io");
            subscriptionHandler.updateSubscription();
        }
    }

    public UUID getOwner() {
        if (permission == Permissions.PRIVATE && coverHolder instanceof MachineCoverContainer mcc) {
            var owner = mcc.getMachine().getOwner();
            return owner != null ? owner.getUUID() : null;
        }
        return null;
    }

    protected boolean isSubscriptionActive() {
        return isWorkingEnabled();
    }

    protected abstract String identifier();

    protected abstract VirtualEntry getEntry();

    protected abstract void setEntry(VirtualEntry entry);

    protected final String getChannelName() {
        return identifier() + this.colorStr;
    }

    protected void setChannelName(String name) {
        if (isRemote()) return;
        VirtualEnderRegistry.getInstance().deleteEntryIf(getOwner(), getEntryType(), getChannelName(),
                VirtualEntry::canRemove);
        this.colorStr = name;
        syncDataHolder.markClientSyncFieldDirty("colorStr");
        setVirtualEntry();
    }

    protected final String getChannelName(VirtualEntry entry) {
        return identifier() + entry.getColorStr();
    }

    protected void setPermission(Permissions permission) {
        if (isRemote()) return;
        VirtualEnderRegistry.getInstance().deleteEntryIf(getOwner(), getEntryType(), getChannelName(),
                VirtualEntry::canRemove);
        this.permission = permission;
        syncDataHolder.markClientSyncFieldDirty("permission");

        setVirtualEntry();
    }

    protected void setVirtualEntry() {
        setEntry(VirtualEnderRegistry.getInstance().getOrCreateEntry(getOwner(), getEntryType(), getChannelName()));
        getEntry().setColor(this.colorStr);
        syncDataHolder.markClientSyncFieldDirty("isAnyChanged");
        this.isAnyChanged = true;
        subscriptionHandler.updateSubscription();
    }

    protected abstract EntryTypes<T> getEntryType();

    protected void update() {
        long timer = coverHolder.getOffsetTimer();
        if (timer % 5 != 0) return;

        if (isWorkingEnabled() && !isRemote()) {
            var entry = VirtualEnderRegistry.getInstance().getOrCreateEntry(getOwner(), getEntryType(),
                    getChannelName());
            if (!entry.getColorStr().equals(this.colorStr)) {
                entry.setColor(this.colorStr);
            }
            if (!getEntry().equals(entry)) {
                setEntry(entry);
            }
            transfer();
        }

        if (isAnyChanged) {
            if (virtualEntryElement != null) virtualEntryElement.update();
            isAnyChanged = false;
        }
        subscriptionHandler.updateSubscription();
    }

    protected abstract void transfer();

    protected void setManualIOMode(ManualIOMode manualIOMode) {
        this.manualIOMode = manualIOMode;
        syncDataHolder.markClientSyncFieldDirty("manualIOMode");
        subscriptionHandler.updateSubscription();
    }

    @Nullable
    protected FilterHandler<?, ?> getFilterHandler() {
        return null;
    }

    protected abstract UIElement addVirtualEntryWidget(VirtualEntry entry, int x, int y, int width, int height,
                                                       boolean canClick);

    protected abstract String getUITitle();

    protected int getColor() {
        return VirtualEntry.parseColor(this.colorStr);
    }

    protected static String entryColorStr(VirtualEntry entry) {
        return entry.getColorStr();
    }

    protected static String entryDescription(VirtualEntry entry) {
        return entry.getDescription();
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putString("colorStr", colorStr);
        tag.putInt("permission", permission.ordinal());
        tag.putInt("io", io.ordinal());
        tag.putInt("manualIO", manualIOMode.ordinal());
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setChannelName(tag.getString("colorStr"));
        setPermission(Permissions.values()[tag.getInt("permission")]);
        setIo(IO.values()[tag.getInt("io")]);
        setManualIOMode(ManualIOMode.values()[tag.getInt("manualIO")]);
        super.pasteConfig(player, tag);
    }

    protected enum Permissions implements EnumSelectorWidget.SelectableEnum {

        PUBLIC("cover.ender_fluid_link.private.tooltip.disabled",
                GuiTextures.BUTTON_PUBLIC_PRIVATE),

        PRIVATE("cover.ender_fluid_link.private.tooltip.enabled",
                GuiTextures.BUTTON_PUBLIC_PRIVATE);

        @Getter
        private final String tooltip;
        @Getter
        private final IGuiTexture icon;

        Permissions(String tooltip, IGuiTexture icon) {
            this.tooltip = tooltip;
            this.icon = icon;
        }
    }

    protected static class VirtualEntryElement extends UIElement {

        private static final int WIDGET_BOARD = 20;
        private static final int GROUP_WIDTH = 176;
        private static final int TOTAL_WIDTH = 156;
        private static final int BUTTON_SIZE = 16;
        private final AbstractEnderLinkCover<?> cover;
        private final MutableBoolean showChannels;
        private final UIElement mainGroup;
        private final UIElement mainChannelGroup;
        private final ScrollerView channelsGroup;
        private final RPCEmitter setChannelRpc;
        private final RPCEmitter requestChannelsRpc;
        private final RPCEmitter clearDescriptionRpc;

        VirtualEntryElement(AbstractEnderLinkCover<?> cover) {
            this.cover = cover;
            this.showChannels = new MutableBoolean(false);
            this.layout(layout -> layout.width(GROUP_WIDTH).height(137));
            mainGroup = element(0, 0, GROUP_WIDTH, 137);
            channelsGroup = new ScrollerView();
            channelsGroup.layout(layout -> layout.left(0).top(20).width(170).height(110));
            channelsGroup.viewPort(view -> view.style(style -> style.backgroundTexture(IGuiTexture.EMPTY)));
            channelsGroup.verticalScroller(scroller -> {
                scroller.scrollContainer(container -> container.style(style -> style.backgroundTexture(ColorPattern.T_GRAY.rectTexture())));
                scroller.scrollBar(bar -> bar.buttonStyle(style -> style
                        .baseTexture(ColorPattern.T_WHITE.rectTexture())
                        .hoverTexture(ColorPattern.T_WHITE.rectTexture())
                        .pressedTexture(ColorPattern.T_WHITE.rectTexture())));
            });
            mainChannelGroup = element(10, 20, 156, 20);
            setChannelRpc = addRPCEvent(RPCEventBuilder.simple(String.class, cover::setChannelName));
            requestChannelsRpc = addRPCEvent(RPCEventBuilder.simple(Boolean.class, CompoundTag.class, this::requestChannels));
            clearDescriptionRpc = addRPCEvent(RPCEventBuilder.simple(String.class, this::clearDescription));
            initWidgets();
        }

        public void update() {
            if (cover.isRemote()) return;
            clearAllChildren();
            mainGroup.clearAllChildren();
            channelsGroup.clearAllScrollViewChildren();
            mainChannelGroup.clearAllChildren();
            initWidgets();
        }

        private void initWidgets() {
            int currentX = 0;
            final var titleGroup = element(10, 5, GROUP_WIDTH, 20);

            this.addChild(titleGroup);
            this.addChild(mainGroup);
            channelsGroup.setVisible(false);
            this.addChild(channelsGroup);

            titleGroup.addChild(createToggleButton());
            titleGroup.addChild(label(15, 3, 130, 10, Component.translatable(cover.getUITitle())));

            var privacyButton = createToggleButtonForPrivacy(currentX);
            mainChannelGroup.addChild(privacyButton);
            currentX += WIDGET_BOARD + 2;
            mainChannelGroup.addChild(createColorBlockElement(currentX));
            currentX += WIDGET_BOARD + 2;
            mainChannelGroup.addChild(createConfirmTextInputElement(currentX));

            mainChannelGroup.addChild(confirmTextInput(0, WIDGET_BOARD + 2, GROUP_WIDTH - WIDGET_BOARD,
                    WIDGET_BOARD, entryDescription(cover.getEntry()), text -> cover.getEntry().setDescription(text),
                    t -> t == null ? "" : t, null, "cover.ender_fluid_link.tooltip.channel_description"));

            mainGroup.addChild(mainChannelGroup);
            mainGroup.addChild(createWorkingEnabledButton());
            addEnumSelectorWidgets();
            mainGroup.addChild(
                    cover.addVirtualEntryWidget(cover.getEntry(), 146, WIDGET_BOARD, WIDGET_BOARD, WIDGET_BOARD, true));

            if (cover.getFilterHandler() != null) {
                mainGroup.addChild(cover.getFilterHandler().createFilterSlotUI(117, 108));
                mainGroup.addChild(cover.getFilterHandler().createFilterConfigUI(10, 72, 156, 60));
            }
        }

        @Contract(" -> new")
        private @NotNull Button createToggleButton() {
            Button button = button(0, 0, 12, 12, GuiTextures.BUTTON_LIST);
            button.setOnClick(event -> {
                showChannels.setValue(!showChannels.getValue());
                mainGroup.setVisible(showChannels.isFalse());
                channelsGroup.setVisible(showChannels.isTrue());
                requestUpdate();
            });
            button.style(style -> style.tooltips("cover.ender_fluid_link.tooltip.list_button"));
            return button;
        }

        @Contract("_ -> new")
        private @NotNull UIElement createToggleButtonForPrivacy(int currentX) {
            return new EnumSelectorWidget<>(currentX, 0,
                    WIDGET_BOARD, WIDGET_BOARD, Permissions.values(), cover.permission, cover::setPermission);
        }

        private ColorBlockElement createColorBlockElement(int currentX) {
            ColorBlockElement element = new ColorBlockElement(cover::getColor);
            element.layout(layout -> layout.left(currentX).top(0).width(WIDGET_BOARD).height(WIDGET_BOARD));
            return element;
        }

        private UIElement createConfirmTextInputElement(int currentX) {
            int GROUP_X = 10;
            int textInputWidth = (GROUP_WIDTH - GROUP_X * 2) - currentX - WIDGET_BOARD - 2;
            return confirmTextInput(currentX, 0, textInputWidth, WIDGET_BOARD, cover.colorStr,
                    cover::setChannelName, text -> {
                        if (text == null || !COLOR_INPUT_PATTERN.matcher(text).matches()) {
                            return VirtualTank.DEFAULT_COLOR;
                        }
                        return text;
                    }, text -> {
                        if (text.length() < 8) {
                            text += "F".repeat(8 - text.length());
                        }
                        return text;
                    }, "cover.ender_fluid_link.tooltip.channel_name");
        }

        @Contract(" -> new")
        private @NotNull Button createWorkingEnabledButton() {
            Button button = button(116, 82, WIDGET_BOARD, WIDGET_BOARD, GuiTextures.BUTTON_POWER);
            button.setOnServerClick(event -> cover.setWorkingEnabled(!cover.isWorkingEnabled()));
            return button;
        }

        private void addEnumSelectorWidgets() {
            mainGroup.addChild(new EnumSelectorWidget<>(146, 82, WIDGET_BOARD, WIDGET_BOARD, List.of(IO.IN, IO.OUT),
                    cover.io, cover::setIo));
            mainGroup.addChild(new EnumSelectorWidget<>(146, 107, WIDGET_BOARD, WIDGET_BOARD, ManualIOMode.VALUES,
                    cover.manualIOMode, cover::setManualIOMode)
                    .style(style -> style.tooltips("cover.universal.manual_import_export.mode.description")));
        }

        private void addChannelWidgets(List<? extends VirtualEntry> entries) {
            channelsGroup.clearAllScrollViewChildren();
            int y = 1;
            for (var entry : entries.stream().sorted(Comparator.comparing(AbstractEnderLinkCover::entryColorStr)).toList()) {
                channelsGroup.addScrollViewChild(createChannelWidget(entry, 10, y,
                        cover.getChannelName(entry).equals(cover.getChannelName())));
                y += 22;
            }
        }

        private @NotNull UIElement createChannelWidget(@NotNull VirtualEntry entry, int x, int y, boolean selected) {
            int currentX = 0;
            int MARGIN = 2;
            int availableWidth = TOTAL_WIDTH - (BUTTON_SIZE + MARGIN) * 3;

            final MutableBoolean canSelect = new MutableBoolean(false);
            var des = entryDescription(entry);
            UIElement channelGroup = element(x, y, TOTAL_WIDTH, BUTTON_SIZE);
            if (selected) {
                channelGroup.style(style -> style.backgroundTexture(ColorPattern.T_GRAY.rectTexture()));
            }
            channelGroup.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (!canSelect.getValue()) return;
                if (cover.getChannelName().equals(cover.getChannelName(entry))) return;
                setChannelRpc.send(entryColorStr(entry));
            });

            // Color block
            ColorBlockElement colorBlockElement = new ColorBlockElement(() -> VirtualEntry.parseColor(entryColorStr(entry)));
            int colorBlockX = currentX;
            colorBlockElement.layout(layout -> layout.left(colorBlockX).top(0).width(BUTTON_SIZE).height(BUTTON_SIZE));
            channelGroup.addChild(colorBlockElement);
            currentX += BUTTON_SIZE + MARGIN;

            // Text box
            channelGroup.addChild(label(BUTTON_SIZE + MARGIN, !des.isEmpty() ? 0 : 4, availableWidth, 8,
                    Component.literal(entryColorStr(entry))));
            currentX += availableWidth + MARGIN;
            if (!des.isEmpty()) {
                var desText = new TextTexture(ChatFormatting.DARK_GRAY + des).setDropShadow(false);
                desText.setType(TextTexture.TextType.ROLL).setRollSpeed(0.7f);
                channelGroup.addChild(textureElement(BUTTON_SIZE + MARGIN, 10, availableWidth, 8, desText));
            }

            // Slot
            UIElement slotElement = cover.addVirtualEntryWidget(entry, currentX, 0, BUTTON_SIZE, BUTTON_SIZE, false);
            channelGroup.addChild(slotElement);
            currentX += BUTTON_SIZE + MARGIN;

            // Clear Description button
            Button clear = button(currentX, 0, BUTTON_SIZE, BUTTON_SIZE, GuiTextures.BUTTON_CLEAR_GRID);
            clear.setOnClick(event -> {
                clearDescriptionRpc.send(cover.getChannelName(entry));
                requestUpdate();
            });
            clear.addEventListener(UIEvents.MOUSE_ENTER, event -> canSelect.setValue(false));
            clear.addEventListener(UIEvents.MOUSE_LEAVE, event -> canSelect.setValue(true));
            clear.style(style -> style.tooltips("cover.ender_fluid_link.tooltip.clear_button"));
            channelGroup.addChild(clear);
            canSelect.setValue(true);

            return channelGroup;
        }

        private void requestUpdate() {
            requestChannelsRpc.send(this::readChannels, showChannels.isTrue());
        }

        private CompoundTag requestChannels(boolean enabled) {
            CompoundTag tag = new CompoundTag();
            if (!enabled) return tag;
            var entries = VirtualEnderRegistry.getInstance().getEntryNames(cover.getOwner(), cover.getEntryType())
                    .stream().map(name -> VirtualEnderRegistry.getInstance().getEntry(cover.getOwner(),
                            cover.getEntryType(), name))
                    .sorted(Comparator.comparing(AbstractEnderLinkCover::entryColorStr))
                    .toList();
            ListTag list = new ListTag();
            for (VirtualEntry entry : entries) {
                list.add(entry.serializeNBT(cover.coverHolder.getLevel().registryAccess()));
            }
            tag.put("entries", list);
            return tag;
        }

        private void readChannels(CompoundTag tag) {
            List<VirtualEntry> entries = new ArrayList<>();
            ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                VirtualEntry entry = cover.getEntryType().createInstance();
                entry.deserializeNBT(cover.coverHolder.getLevel().registryAccess(), list.getCompound(i));
                entries.add(entry);
            }
            addChannelWidgets(entries);
        }

        private void clearDescription(String channelName) {
            VirtualEnderRegistry.getInstance().getEntry(cover.getOwner(), cover.getEntryType(), channelName)
                    .setDescription("");
        }

        private static UIElement element(int x, int y, int width, int height) {
            return new UIElement().layout(layout -> layout.left(x).top(y).width(width).height(height));
        }

        private static Label label(int x, int y, int width, int height, Component text) {
            Label label = new Label();
            label.setValue(text);
            label.layout(layout -> layout.left(x).top(y).width(width).height(height));
            label.textStyle(style -> style.textColor(0x404040).textShadow(false));
            return label;
        }

        private static UIElement textureElement(int x, int y, int width, int height, IGuiTexture texture) {
            return element(x, y, width, height).style(style -> style.backgroundTexture(texture));
        }

        private static Button button(int x, int y, int width, int height, IGuiTexture icon) {
            Button button = new Button().noText();
            button.layout(layout -> layout.left(x).top(y).width(width).height(height));
            IGuiTexture texture = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, icon);
            button.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
            return button;
        }

        private static UIElement confirmTextInput(int x, int y, int width, int height, String text,
                                                  java.util.function.Consumer<String> textResponder,
                                                  @Nullable java.util.function.Function<String, String> validator,
                                                  @Nullable java.util.function.Function<String, String> returnValidator,
                                                  String tooltip) {
            UIElement group = element(x, y, width, height);
            TextField textField = new TextField();
            textField.layout(layout -> layout.left(1).top(1).width(width - height - 4).height(height - 2));
            textField.style(style -> style.background(GuiTextures.DISPLAY).tooltips(tooltip));
            textField.textFieldStyle(style -> style.textColor(0x404040).textShadow(false));
            textField.setAnyString();
            if (validator != null) {
                textField.setTextValidator(candidate -> Objects.equals(validator.apply(candidate), candidate));
            }
            textField.setText(text, false);
            group.addChild(textField);
            RPCEmitter confirmRpc = group.addRPCEvent(RPCEventBuilder.simple(String.class, value -> {
                if (returnValidator != null) {
                    value = returnValidator.apply(value);
                }
                textResponder.accept(value);
            }));
            Button confirm = button(width - height, 0, height, height, GuiTextures.BUTTON_CHECK);
            confirm.setOnClick(event -> confirmRpc.send(textField.getValue()));
            group.addChild(confirm);
            return group;
        }

        private static class ColorBlockElement extends UIElement {

            private static boolean showAlpha = false;
            private final java.util.function.IntSupplier colorSupplier;

            private ColorBlockElement(java.util.function.IntSupplier colorSupplier) {
                this.colorSupplier = colorSupplier;
                addEventListener(UIEvents.MOUSE_DOWN, event -> showAlpha = !showAlpha);
            }

            @Override
            public void drawBackgroundAdditional(GUIContext guiContext) {
                int color = colorSupplier.getAsInt();
                int opaqueColor = showAlpha ? color : color | 0xFF000000;
                guiContext.graphics.fill((int) getPositionX() + 1, (int) getPositionY() + 1,
                        (int) (getPositionX() + getSizeWidth() - 1),
                        (int) (getPositionY() + getSizeHeight() - 1), opaqueColor);
                guiContext.graphics.renderOutline((int) getPositionX() + 1, (int) getPositionY() + 1,
                        (int) getSizeWidth() - 2, (int) getSizeHeight() - 2, 0xFF000000);
                super.drawBackgroundAdditional(guiContext);
            }
        }
    }
}
