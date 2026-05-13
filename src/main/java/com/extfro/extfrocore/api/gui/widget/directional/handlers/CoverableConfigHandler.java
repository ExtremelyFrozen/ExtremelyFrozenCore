package com.extfro.extfrocore.api.gui.widget.directional.handlers;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.ConfiguratorPanel;
import com.extfro.extfrocore.api.gui.fancy.FancyMachineUIWidget;
import com.extfro.extfrocore.api.gui.widget.CoverConfigurator;
import com.extfro.extfrocore.api.gui.widget.directional.IDirectionalConfigHandler;
import com.extfro.extfrocore.api.item.IComponentItem;
import com.extfro.extfrocore.api.item.component.IItemComponent;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;
import com.extfro.extfrocore.common.item.behavior.CoverPlaceBehavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.ClickData;

public class CoverableConfigHandler implements IDirectionalConfigHandler {

    private static final IGuiTexture CONFIG_BTN_TEXTURE = new GuiTextureGroup(GuiTextures.IO_CONFIG_COVER_SETTINGS);

    private final ICoverable machine;
    private CustomItemStackHandler handler;
    private Direction side;

    private ConfiguratorPanel panel;
    private ConfiguratorPanel.FloatingTab coverConfigurator;

    private ItemSlot slotElement;
    private Button configButton;
    private CoverBehavior coverBehavior;

    public CoverableConfigHandler(ICoverable machine) {
        this.machine = machine;
        this.handler = createItemStackHandler();
    }

    private CustomItemStackHandler createItemStackHandler() {
        var handler = new CustomItemStackHandler(1) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };

        handler.setFilter(itemStack -> {
            if (itemStack.isEmpty()) return true;
            if (this.side == null) return false;
            return CoverPlaceBehavior.isCoverBehaviorItem(itemStack, () -> false,
                    coverDef -> ICoverable.canPlaceCover(coverDef, this.machine) &&
                            coverDef.createCoverBehavior(this.machine, this.side).canAttach());
        });

        return handler;
    }

    @Override
    public UIElement getSideSelectorElement(Scene scene, FancyMachineUIWidget machineUI) {
        UIElement group = new UIElement().layout(layout -> layout.width((18 * 2) + 1).height(18));
        this.panel = machineUI.getConfiguratorPanel();

        slotElement = new ItemSlot().bind(handler, 0);
        slotElement.layout(layout -> layout.left(19).top(0).width(18).height(18));
        slotElement.style(style -> style.background(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.IO_CONFIG_COVER_SLOT_OVERLAY)));
        slotElement.slotStyle(style -> style.acceptQuickMove(false));
        slotElement.addServerEventListener(UIEvents.MOUSE_UP, event -> coverItemChanged());
        group.addChild(slotElement);

        configButton = new Button().noText();
        configButton.layout(layout -> layout.left(0).top(0).width(18).height(18));
        configButton.buttonStyle(style -> style
                .baseTexture(CONFIG_BTN_TEXTURE)
                .hoverTexture(CONFIG_BTN_TEXTURE)
                .pressedTexture(CONFIG_BTN_TEXTURE));
        configButton.setOnServerClick(event -> toggleConfigTab(new ClickData()));
        group.addChild(configButton);

        checkCoverBehaviour();
        updateWidgetVisibility();

        return group;
    }

    // FIXME: This gets called twice in a single tick, causing two covers to exist simultaneously
    private void coverItemChanged() {
        closeConfigTab();

        if (panel.getModularUI() == null || !(panel.getModularUI().player instanceof ServerPlayer serverPlayer) || side == null)
            return;

        var item = handler.getStackInSlot(0);
        if (machine.getCoverAtSide(side) != null) {
            machine.removeCover(false, side, serverPlayer);
        }

        if (!item.isEmpty() && machine.getCoverAtSide(side) == null) {
            if (item.getItem() instanceof IComponentItem componentItem) {
                for (IItemComponent component : componentItem.getComponents()) {
                    if (component instanceof CoverPlaceBehavior placeBehavior) {
                        machine.placeCoverOnSide(side, item, placeBehavior.coverDefinition(), serverPlayer);
                        break;
                    }
                }
            }
        }

        checkCoverBehaviour();
    }

    @Override
    public void onSideSelected(BlockPos pos, Direction side) {
        this.side = side;
        checkCoverBehaviour();
        closeConfigTab();
    }

    private void updateWidgetVisibility() {
        var sideSelected = this.side != null;
        if (slotElement != null) {
            slotElement.setVisible(sideSelected);
            slotElement.setActive(sideSelected);
        }
        if (configButton != null) {
            boolean hasUICover = sideSelected && coverBehavior != null && machine.getCoverAtSide(side) instanceof IUICover;
            configButton.setVisible(hasUICover);
            configButton.setActive(hasUICover);
        }
    }

    public void checkCoverBehaviour() {
        if (side == null)
            return;

        var coverBehaviour = machine.getCoverAtSide(side);
        if (coverBehaviour != this.coverBehavior) {
            this.coverBehavior = coverBehaviour;

            var attachItem = coverBehaviour == null ? ItemStack.EMPTY : coverBehaviour.getAttachItem();
            handler.setStackInSlot(0, attachItem);
            handler.onContentsChanged(0);
        }

        updateWidgetVisibility();
    }

    private void toggleConfigTab(ClickData cd) {
        if (this.coverConfigurator == null)
            openConfigTab();
        else
            closeConfigTab();
    }

    private void openConfigTab() {
        CoverConfigurator configurator = new CoverConfigurator(this.machine, this.side, this.coverBehavior) {

            @Override
            public Component getTitle() {
                // Uses the widget's own title
                return Component.empty();
            }

            @Override
            public IGuiTexture getIcon() {
                return GuiTextures.CLOSE_ICON;
            }

            @Override
            public UIElement createConfigurator() {
                UIElement group = new UIElement();

                if (side == null || !(coverable.getCoverAtSide(side) instanceof IUICover iuiCover))
                    return group;

                UIElement coverConfigurator = iuiCover.createUIElement();
                int width = Math.max(120, iuiCover.getUIWidth());
                int height = Math.max(80, iuiCover.getUIHeight());
                group.layout(layout -> layout.width(width).height(height));
                coverConfigurator.layout(layout -> layout.left((width - iuiCover.getUIWidth()) / 2f).top(0)
                        .width(iuiCover.getUIWidth()).height(iuiCover.getUIHeight()));

                group.addChild(coverConfigurator);
                return group;
            }
        };

        this.coverConfigurator = this.panel.createFloatingTab(configurator);
        this.panel.getTabs().add(this.coverConfigurator);
        this.panel.addChild(this.coverConfigurator);
        this.panel.expandTab(this.coverConfigurator);

        coverConfigurator.onClose(() -> {
            if (coverConfigurator != null) {
                this.panel.removeTab(this.coverConfigurator);
            }

            this.coverConfigurator = null;
        });
    }

    private void closeConfigTab() {
        if (this.coverConfigurator != null) {
            this.panel.collapseTab();
        }
    }

    @Override
    public ScreenSide getScreenSide() {
        return ScreenSide.RIGHT;
    }
}
