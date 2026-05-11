package com.extfro.extfrocore.api.item;

import com.extfro.extfrocore.api.block.MetaMachineBlock;
import com.extfro.extfrocore.api.machine.MachineDefinition;

import net.minecraft.world.item.BlockItem;

public class MetaMachineItem extends BlockItem {

    public MetaMachineItem(MetaMachineBlock block, Properties properties) {
        super(block, properties);
    }

    public MachineDefinition getDefinition() {
        return ((MetaMachineBlock) getBlock()).getDefinition();
    }
}
