package com.extfro.extfrocore.api.gui.widget.directional;

import com.extfro.extfrocore.api.gui.fancy.FancyMachineUIWidget;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.gui.util.ClickData;
import com.lowdragmc.lowdraglib2.utils.data.BlockPosFace;

public interface IDirectionalConfigHandler {

    /**
     * Returns the buttons to display inside the side selector
     */
    UIElement getSideSelectorElement(Scene scene, FancyMachineUIWidget machineUI);

    /**
     * Called whenever a side is selected in the side selector GUI
     */
    void onSideSelected(BlockPos pos, Direction side);

    /**
     * Determines which side of the screen the UI element should be placed on.
     */
    ScreenSide getScreenSide();

    enum ScreenSide {
        LEFT,
        RIGHT,
    }

    default void handleClick(ClickData cd, Direction direction) {
        // Do nothing by default
    }

    default void handleClick(int button, Direction direction) {
        handleClick(new ClickData(), direction);
    }

    @OnlyIn(Dist.CLIENT)
    default void renderOverlay(Scene scene, BlockPosFace blockPosFace) {
        // Do nothing by default
    }

    default void addAdditionalUIElements(UIElement parent) {
        // Do nothing by default
    }
}
