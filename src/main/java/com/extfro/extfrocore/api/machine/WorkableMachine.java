package com.extfro.extfrocore.api.machine;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.IRecipeHandler;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.machine.feature.IRecipeLogicMachine;
import com.extfro.extfrocore.api.machine.trait.RecipeHandlerList;
import com.extfro.extfrocore.api.machine.trait.RecipeLogic;
import com.extfro.extfrocore.api.recipe.MachineRecipeType;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public abstract class WorkableMachine extends MetaMachine implements IRecipeLogicMachine {

    @Getter
    @SaveField
    @SyncToClient
    protected final RecipeLogic recipeLogic;
    @Getter
    @SaveField
    @SyncToClient
    protected int activeRecipeType;
    private final Map<IO, List<RecipeHandlerList>> capabilitiesProxy = new Reference2ObjectOpenHashMap<>();
    private final Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> capabilitiesFlat = new Reference2ObjectOpenHashMap<>();

    public WorkableMachine(BlockEntityCreationInfo info) {
        super(info);
        recipeLogic = new RecipeLogic(this);
        activeRecipeType = 0;
    }

    @Override
    public @NotNull MachineRecipeType[] getRecipeTypes() {
        return getDefinition().getRecipeTypes();
    }

    @Override
    public @NotNull MachineRecipeType getRecipeType() {
        MachineRecipeType[] recipeTypes = getRecipeTypes();
        if (recipeTypes.length == 0) {
            throw new IllegalStateException("Machine has no recipe type: " + getDefinition().getId());
        }
        if (activeRecipeType >= recipeTypes.length) {
            activeRecipeType = recipeTypes.length - 1;
        }
        return recipeTypes[activeRecipeType];
    }

    @Override
    public void setActiveRecipeType(int type) {
        MachineRecipeType[] recipeTypes = getRecipeTypes();
        activeRecipeType = recipeTypes.length == 0 ? 0 : Math.clamp(type, 0, recipeTypes.length - 1);
        recipeLogic.updateTickSubscription();
    }

    @Override
    public @NotNull Map<IO, List<RecipeHandlerList>> getCapabilitiesProxy() {
        return capabilitiesProxy;
    }

    @Override
    public @NotNull Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> getCapabilitiesFlat() {
        return capabilitiesFlat;
    }
}
