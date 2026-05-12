package com.extfro.extfrocore.common.item;

import com.extfro.extfrocore.api.block.MetaMachineBlock;
import com.extfro.extfrocore.api.data.chemical.material.Material;
import com.extfro.extfrocore.api.data.chemical.material.properties.FluidPipeProperties;
import com.extfro.extfrocore.api.data.chemical.material.properties.PropertyKey;
import com.extfro.extfrocore.api.item.MetaMachineItem;
import com.extfro.extfrocore.api.misc.forge.ThermalFluidHandlerItemStack;
import com.extfro.extfrocore.common.data.item.GTDataComponents;
import com.extfro.extfrocore.common.data.machines.GTMachineUtils;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

import org.jetbrains.annotations.NotNull;

public class DrumMachineItem extends MetaMachineItem {

    @NotNull
    private final Material mat;

    protected DrumMachineItem(MetaMachineBlock block, Properties properties, @NotNull Material mat) {
        super(block, properties);
        this.mat = mat;
    }

    public static DrumMachineItem create(MetaMachineBlock block, Properties properties, @NotNull Material mat) {
        return new DrumMachineItem(block, properties, mat);
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        if (mat.hasProperty(PropertyKey.FLUID_PIPE)) {
            FluidPipeProperties property = mat.getProperty(PropertyKey.FLUID_PIPE);
            event.registerItem(Capabilities.FluidHandler.ITEM,
                    (stack, ignored) -> new ThermalFluidHandlerItemStack(stack,
                            GTMachineUtils.DRUM_CAPACITY.getInt(getDefinition()),
                            property.getMaxFluidTemperature(), property.isGasProof(), property.isAcidProof(),
                            property.isCryoProof(),
                            property.isPlasmaProof()),
                    this);
        } else {
            event.registerItem(Capabilities.FluidHandler.ITEM,
                    (stack, ignored) -> new FluidHandlerItemStack(GTDataComponents.FLUID_CONTENT, stack,
                            GTMachineUtils.DRUM_CAPACITY.getInt(getDefinition())),
                    this);
        }
    }
}
