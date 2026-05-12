package com.extfro.extfrocore.integration.kjs.helpers;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.machine.multiblock.MultiblockControllerMachine;
import com.extfro.extfrocore.common.machine.multiblock.electric.FusionReactorMachine;
import com.extfro.extfrocore.common.machine.multiblock.generator.LargeCombustionEngineMachine;
import com.extfro.extfrocore.common.machine.multiblock.generator.LargeTurbineMachine;
import com.extfro.extfrocore.common.machine.multiblock.steam.SteamParallelMultiblockMachine;

/**
 * Collection of functions that can be used in the Machine Creation Functions in KJS definitions
 * Makes using them for KJS easier, as loading the relevant class won't be required.
 */
@SuppressWarnings("unused")
public final class MachineConstructors {

    // This one in particular stops a crash when trying to define a new LCE
    // The crash is caused by the static FluidStack members in LargeCombustionEngine.class
    public static MultiblockControllerMachine createLargeCombustionEngine(BlockEntityCreationInfo info, int tier) {
        return new LargeCombustionEngineMachine(info, tier);
    }

    public static MultiblockControllerMachine createLargeTurbine(BlockEntityCreationInfo info, int tier) {
        return new LargeTurbineMachine(info, tier);
    }

    public static MultiblockControllerMachine createFusionReactor(BlockEntityCreationInfo info, int tier) {
        return new FusionReactorMachine(info, tier);
    }

    public static MultiblockControllerMachine createSteamMultiblock(BlockEntityCreationInfo info, int parallels) {
        return new SteamParallelMultiblockMachine(info, parallels);
    }
}
