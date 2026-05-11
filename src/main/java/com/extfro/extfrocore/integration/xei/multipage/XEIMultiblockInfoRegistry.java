package com.extfro.extfrocore.integration.xei.multipage;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MultiblockMachineDefinition;
import com.extfro.extfrocore.api.registry.EFRegistries;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public final class XEIMultiblockInfoRegistry {

    public static final ResourceLocation CATEGORY_ID = ExtForCore.id("multiblock_info");
    public static final Component CATEGORY_TITLE = Component.translatable("extfrocore.xei.multiblock_info");

    private XEIMultiblockInfoRegistry() {}

    @Unmodifiable
    public static List<MultiblockInfoDisplay> getAllDisplays() {
        List<MultiblockInfoDisplay> displays = new ArrayList<>();
        for (MachineDefinition machine : EFRegistries.MACHINES) {
            if (machine instanceof MultiblockMachineDefinition multiblock && multiblock.isRenderXEIPreview()) {
                MultiblockInfoDisplay display = MultiblockInfoDisplay.fromDefinition(multiblock);
                if (display.hasPages()) {
                    displays.add(display);
                }
            }
        }
        return List.copyOf(displays);
    }
}
