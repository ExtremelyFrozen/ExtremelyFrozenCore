package com.extfro.extfrocore.common.machine.multiblock.part;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.machine.multiblock.CleanroomType;
import com.extfro.extfrocore.api.machine.multiblock.MultiblockControllerMachine;
import com.extfro.extfrocore.common.machine.trait.CleanroomProviderTrait;
import com.extfro.extfrocore.common.machine.trait.CleanroomReceiverTrait;

import lombok.Getter;

import java.util.Set;

import static com.extfro.extfrocore.api.EFValues.UHV;
import static com.extfro.extfrocore.api.EFValues.UV;

public class CleaningMaintenanceHatchPartMachine extends AutoMaintenanceHatchPartMachine {

    private final CleanroomProviderTrait cleanroomProvider;

    @Getter
    private final CleanroomType cleanroomType;

    public CleaningMaintenanceHatchPartMachine(BlockEntityCreationInfo info, CleanroomType cleanroomType) {
        super(info);
        this.cleanroomType = cleanroomType;
        this.cleanroomProvider = attachTrait(new CleanroomProviderTrait(Set.of(cleanroomType)));
        cleanroomProvider.setActive(true);
    }

    @Override
    public void addedToController(MultiblockControllerMachine controller) {
        super.addedToController(controller);
        controller.self().getTraitOptional(CleanroomReceiverTrait.TYPE)
                .ifPresent(t -> t.setCleanroomProvider(cleanroomProvider));
    }

    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
        super.removedFromController(controller);
        controller.self().getTraitOptional(CleanroomReceiverTrait.TYPE)
                .ifPresent(CleanroomReceiverTrait::removeCleanroom);
    }

    @Override
    public int getTier() {
        return cleanroomType == CleanroomType.CLEANROOM ? UV : UHV;
    }
}
