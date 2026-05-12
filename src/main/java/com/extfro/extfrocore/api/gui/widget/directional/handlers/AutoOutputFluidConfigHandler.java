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

public class AutoOutputFluidConfigHandler implements IDirectionalConfigHandler {

    private static final IGuiTexture TEXTURE_OFF = new GuiTextureGroup(
            GuiTextures.VANILLA_BUTTON,
            CroppedTexture.of("gtceu:textures/gui/icon/io_config/output_config_fluid_modes.png", 0, 0, 1, 1 / 3f));
    private static final IGuiTexture TEXTURE_OUTPUT = new GuiTextureGroup(
            GuiTextures.VANILLA_BUTTON,
            CroppedTexture.of("gtceu:textures/gui/icon/io_config/output_config_fluid_modes.png", 0, 1 / 3f, 1, 1 / 3f));
    private static final IGuiTexture TEXTURE_AUTO = new GuiTextureGroup(
            GuiTextures.VANILLA_BUTTON,
            CroppedTexture.of("gtceu:textures/gui/icon/io_config/output_config_fluid_modes.png", 0, 2 / 3f, 1, 1 / 3f));
    private static final IGuiTexture ALLOW_INPUT_OFF = new GuiTextureGroup(
            CroppedTexture.of("gtceu:textures/gui/widget/toggle_button_background.png", 0, 0, 1, 0.5f),
            GuiTextures.BUTTON_FLUID_OUTPUT);
    private static final IGuiTexture ALLOW_INPUT_ON = new GuiTextureGroup(
            CroppedTexture.of("gtceu:textures/gui/widget/toggle_button_background.png", 0, 0.5f, 1, 0.5f),
            GuiTextures.BUTTON_FLUID_OUTPUT);

    private final AutoOutputTrait trait;
    private Direction side;
    private Button ioModeButton;
    private Button allowInputButton;

    public AutoOutputFluidConfigHandler(AutoOutputTrait trait) {
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
            trait.setAllowFluidInputFromOutputSide(!trait.allowsFluidInputFromOutputSide());
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
                tooltips = LangHandler.getMultiLang("gtceu.gui.fluid_auto_output.unselected").toArray(Component[]::new);
            } else if (trait.getFluidOutputDirection() == side) {
                if (trait.isAutoOutputFluids()) {
                    texture = TEXTURE_AUTO;
                    tooltips = new Component[] { Component.translatable("gtceu.gui.fluid_auto_output.enabled") };
                } else {
                    texture = TEXTURE_OUTPUT;
                    tooltips = new Component[] { Component.translatable("gtceu.gui.fluid_auto_output.disabled") };
                }
            } else {
                texture = TEXTURE_OFF;
                tooltips = LangHandler.getMultiLang("gtceu.gui.fluid_auto_output.other_direction")
                        .toArray(Component[]::new);
            }
            ioModeButton.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
            ioModeButton.style(style -> style.tooltips(tooltips));
        }
        if (allowInputButton != null) {
            IGuiTexture texture = trait.allowsFluidInputFromOutputSide() ? ALLOW_INPUT_ON : ALLOW_INPUT_OFF;
            allowInputButton.buttonStyle(style -> style
                    .baseTexture(texture)
                    .hoverTexture(texture)
                    .pressedTexture(texture));
            allowInputButton.style(style -> style.tooltips(Component.translatable(
                    "gtceu.gui.fluid_auto_output.allow_input." +
                            (trait.allowsFluidInputFromOutputSide() ? "enabled" : "disabled"))));
        }
    }

    private void onIOModePressed() {
        if (this.side == null)
            return;

        if (trait.getFluidOutputDirection() == this.side) {
            trait.setAllowAutoOutputFluids(!trait.isAutoOutputFluids());
        } else {
            trait.setAllowAutoOutputFluids(false);
            trait.setFluidOutputDirection(this.side);
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
        if (!canHandleClick(button) || !trait.supportsAutoOutputFluids())
            return;

        if (trait.getFluidOutputDirection() != direction) {
            trait.setFluidOutputDirection(direction);
            trait.setAllowAutoOutputFluids(false);
        } else {
            trait.setAllowAutoOutputFluids(!trait.isAutoOutputFluids());
        }
        updateButtons();
    }

    @SuppressWarnings("RedundantIfStatement") // Cleaner code this way
    private boolean canHandleClick(int button) {
        if (button == 1)
            return true;

        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderOverlay(Scene scene, BlockPosFace blockPosFace) {
        if (trait.getFluidOutputDirection() != blockPosFace.facing())
            return;

        scene.drawFacingBorder(new PoseStack(), blockPosFace,
                trait.isAutoOutputFluids() ? 0xff00b4ff : 0x8f00b4ff, 2);
    }

    @Override
    public void addAdditionalUIElements(UIElement parent) {
        Label text = new Label() {

            @Override
            public void screenTick() {
                super.screenTick();
                setVisible(trait.isAutoOutputFluids() && trait.getFluidOutputDirection() != null);
            }
        };
        text.setValue(Component.translatable("gtceu.gui.auto_output.name"));
        text.layout(layout -> layout.left(parent.getSizeWidth() - 84).top(4).width(80).height(10));
        text.textStyle(style -> style.textColor(0xff00b4ff).textShadow(false));
        parent.addChild(text);
    }
}
