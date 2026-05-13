package com.extfro.extfrocore.api.gui.widget.directional;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.FancyMachineUIWidget;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.utils.GTUtil;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib2.client.scene.ISceneBlockRenderHook;
import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.math.Size;
import com.lowdragmc.lowdraglib2.utils.data.BlockPosFace;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CombinedDirectionalConfigurator extends UIElement {

    protected final IDirectionalConfigHandler[] configHandlers;
    protected final int width, height;
    private final FancyMachineUIWidget machineUI;
    private final MetaMachine machine;

    protected Scene scene;

    protected @Nullable BlockPos selectedPos;
    protected @Nullable Direction selectedSide;

    public CombinedDirectionalConfigurator(FancyMachineUIWidget machineUI, IDirectionalConfigHandler[] configHandlers,
                                           MetaMachine machine, int width, int height) {
        layout(layout -> layout.width(width).height(height));
        this.width = width;
        this.height = height;

        this.machineUI = machineUI;
        this.configHandlers = configHandlers;
        this.machine = machine;
        buildUI();
    }

    private void buildUI() {
        style(style -> style.background(GuiTextures.BACKGROUND_INVERSE));
        addChild(scene = createScene());

        for (IDirectionalConfigHandler configHandler : configHandlers) {
            configHandler.addAdditionalUIElements(this);
        }

        addConfigElements(scene);
    }

    private Scene createScene() {
        var pos = this.machine.getBlockPos();

        Scene scene = new Scene();
        scene.layout(layout -> layout.left(4).top(4).width(width - 8).height(height - 8));
        scene.createScene(this.machine.getLevel(), false, Size.of(width - 8, height - 8))
                .setRenderedCore(List.of(pos), null)
                .setRenderSelect(false)
                .setIntractable(true)
                .setOnSelected(this::onSideSelected);

        scene.style(style -> style.background(ColorPattern.BLACK.rectTexture()));
        scene.addServerEventListener(UIEvents.MOUSE_DOWN, this::handleSceneClick);
        scene.getRenderer().addRenderedBlocks(
                List.of(pos.above(), pos.below(), pos.north(), pos.south(), pos.east(), pos.west()),
                new ISceneBlockRenderHook() {

                    @Override
                    @OnlyIn(Dist.CLIENT)
                    public void apply(RenderType layer) {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                    }
                });

        scene.setAfterWorldRender(this::renderOverlays);
        return scene;
    }

    private void renderOverlays(Scene scene) {
        for (Direction face : GTUtil.DIRECTIONS) {
            for (IDirectionalConfigHandler configHandler : configHandlers) {
                configHandler.renderOverlay(scene, new BlockPosFace(machine.getBlockPos(), face));
            }
        }
    }

    private void addConfigElements(Scene scene) {
        int yOffsetLeft = 0, yOffsetRight = 0;

        for (IDirectionalConfigHandler configHandler : configHandlers) {
            UIElement element = configHandler.getSideSelectorElement(scene, machineUI);

            if (element == null)
                continue;

            final int elementWidth = (int) element.getSizeWidth();
            final int elementHeight = (int) element.getSizeHeight();
            switch (configHandler.getScreenSide()) {
                case LEFT -> {
                    int top = height - 6 - elementHeight - yOffsetLeft;
                    element.layout(layout -> layout.left(6).top(top));
                    yOffsetLeft += elementHeight + 3;
                }
                case RIGHT -> {
                    int left = width - elementWidth - 6;
                    int top = height - 6 - elementHeight - yOffsetRight;
                    element.layout(layout -> layout.left(left).top(top));
                    yOffsetRight += elementHeight + 3;
                }
            }

            this.addChild(element);
        }
    }

    protected void onSideSelected(BlockPos pos, Direction side) {
        if (!pos.equals(machine.getBlockPos()))
            return;

        if (this.selectedSide == side)
            return; // No need to do anything if the same side is already selected

        this.selectedSide = side;

        for (IDirectionalConfigHandler configWidget : this.configHandlers) {
            configWidget.onSideSelected(pos, side);
        }
    }

    private void handleSceneClick(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event) {
        if (this.selectedSide == null) {
            return;
        }
        var hover = scene.getLastHoverPosFace();
        if (hover == null || !hover.pos().equals(machine.getBlockPos()) || hover.facing() != this.selectedSide) {
            return;
        }
        for (IDirectionalConfigHandler configHandler : configHandlers) {
            configHandler.handleClick(event.button, this.selectedSide);
        }
    }
}
