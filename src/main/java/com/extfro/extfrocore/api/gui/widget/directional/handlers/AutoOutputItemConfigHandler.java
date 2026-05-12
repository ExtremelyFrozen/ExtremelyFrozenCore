package com.extfro.extfrocore.api.gui.widget.directional.handlers;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.FancyMachineUIWidget;
import com.extfro.extfrocore.api.gui.texture.CroppedTexture;
import com.extfro.extfrocore.api.gui.widget.directional.IDirectionalConfigHandler;
import com.extfro.extfrocore.api.machine.trait.AutoOutputTrait;
import com.extfro.extfrocore.data.lang.LangHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.utils.data.BlockPosFace;
import com.mojang.blaze3d.vertex.PoseStack;

public class AutoOutputItemConfigHandler implements IDirectionalConfigHandler {

    private static final IGuiTexture TEXTURE_OFF = new GuiTextureGroup(
            GuiTextures.VANILLA_BUTTON,
            CroppedTexture.of("gtceu:textures/gui/icon/io_config/output_config_item_modes.png", 0, 0, 1, 1 / 3f));
    private static final IGuiTexture TEXTURE_OUTPUT = new GuiTextureGroup(
            GuiTextures.VANILLA_BUTTON,
            CroppedTexture.of("gtceu:textures/gui/icon/io_config/output_config_item_modes.png", 0, 1 / 3f, 1, 1 / 3f));
    private static final IGuiTexture TEXTURE_AUTO = new GuiTextureGroup(
            GuiTextures.VANILLA_BUTTON,
            CroppedTexture.of("gtceu:textures/gui/icon/io_config/output_config_item_modes.png", 0, 2 / 3f, 1, 1 / 3f));
    private static final IGuiTexture ALLOW_INPUT_OFF = new GuiTextureGroup(
            CroppedTexture.of("gtceu:textures/gui/widget/toggle_button_background.png", 0, 0, 1, 0.5f),
            GuiTextures.BUTTON_ITEM_OUTPUT);
    private static final IGuiTexture ALLOW_INPUT_ON = new GuiTextureGroup(
            CroppedTexture.of("gtceu:textures/gui/widget/toggle_button_background.png", 0, 0.5f, 1, 0.5f),
            GuiTextures.BUTTON_ITEM_OUTPUT);

    private final AutoOutputTrait trait;
    private Direction side;
    private Button ioModeButton;
    private Button allowInputButton;

    public AutoOutputItemConfigHandler(AutoOutputTrait trait) {
        this.trait = trait;
    }

    @Override
    public UIElement getSideSelectorElement(Scene scene, FancyMachineUIWidget machineUI) {
        UIElement group = new UIElement().layout(layout -> layout.width((18 * 2) + 1).height(18));

        ioModeButton = new Button().noText();
        ioModeButton.layout(layout -> layout.left(0).top(0).width(18).height(18));
        ioModeButton.setOnServerClick(event -> {
            onIOModePressed();
            updateButtons();
        });
        group.addChild(ioModeButton);

        allowInputButton = new Button().noText();
        allowInputButton.layout(layout -> layout.left(19).top(0).width(18).height(18));
        allowInputButton.setOnServerClick(event -> {
            trait.setAllowItemInputFromOutputSide(!trait.allowsItemInputFromOutputSide());
            updateButtons();
        });
        group.addChild(allowInputButton);

        updateButtons();
        return group;
    }

    private void updateButtons() {
        if (ioModeButton != null) {
            IGuiTexture texture;
            Component[] tooltips;
            if (side == null) {
                texture = TEXTURE_OFF;
                tooltips = LangHandler.getMultiLang("gtceu.gui.item_auto_output.unselected").toArray(Component[]::new);
            } else if (trait.getItemOutputDirection() == side) {
                if (trait.isAutoOutputItems()) {
                    texture = TEXTURE_AUTO;
                    tooltips = new Component[] { Component.translatable("gtceu.gui.item_auto_output.enabled") };
                } else {
                    texture = TEXTURE_OUTPUT;
                    tooltips = new Component[] { Component.translatable("gtceu.gui.item_auto_output.disabled") };
                }
            } else {
                texture = TEXTURE_OFF;
                tooltips = LangHandler.getMultiLang("gtceu.gui.item_auto_output.other_direction")
                        .toArray(Component[]::new);
            }
            ioModeButton.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
            ioModeButton.style(style -> style.tooltips(tooltips));
        }
        if (allowInputButton != null) {
            IGuiTexture texture = trait.allowsItemInputFromOutputSide() ? ALLOW_INPUT_ON : ALLOW_INPUT_OFF;
            allowInputButton.buttonStyle(style -> style
                    .baseTexture(texture)
                    .hoverTexture(texture)
                    .pressedTexture(texture));
            allowInputButton.style(style -> style.tooltips(Component.translatable(
                    "gtceu.gui.item_auto_output.allow_input." +
                            (trait.allowsItemInputFromOutputSide() ? "enabled" : "disabled"))));
        }
    }

    private void onIOModePressed() {
        if (this.side == null)
            return;

        if (trait.getItemOutputDirection() == this.side) {
            trait.setAllowAutoOutputItems(!trait.isAutoOutputItems());
        } else {
            trait.setAllowAutoOutputItems(false);
            trait.setItemOutputDirection(this.side);
        }
    }

    @Override
    public void onSideSelected(BlockPos pos, Direction side) {
        this.side = side;
        updateButtons();
    }

    @Override
    public ScreenSide getScreenSide() {
        return ScreenSide.LEFT;
    }

    @Override
    public void handleClick(int button, Direction direction) {
        if (!canHandleClick(button) || !trait.supportsAutoOutputItems())
            return;

        if (trait.getItemOutputDirection() != direction) {
            trait.setItemOutputDirection(direction);
            trait.setAllowAutoOutputItems(false);
        } else {
            trait.setAllowAutoOutputItems(!trait.isAutoOutputItems());
        }
        updateButtons();
    }

    @SuppressWarnings("RedundantIfStatement") // Cleaner code this way
    private boolean canHandleClick(int button) {
        if (button == 0)
            return true;

        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderOverlay(Scene scene, BlockPosFace blockPosFace) {
        if (trait.getItemOutputDirection() != blockPosFace.facing())
            return;

        scene.drawFacingBorder(new PoseStack(), blockPosFace,
                trait.isAutoOutputItems() ? 0xffff6e0f : 0x8fff6e0f, 1);
    }

    @Override
    public void addAdditionalUIElements(UIElement parent) {
        Label text = new Label() {

            @Override
            public void screenTick() {
                super.screenTick();
                setVisible(trait.isAutoOutputItems() && trait.getItemOutputDirection() != null);
            }
        };
        text.setValue(Component.translatable("gtceu.gui.auto_output.name"));
        text.layout(layout -> layout.left(4).top(4).width(80).height(10));
        text.textStyle(style -> style.textColor(0xffff6e0f).textShadow(false));
        parent.addChild(text);
    }
}
