package com.extfro.extfrocore.api.machine.trait;

import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.TickableSubscription;
import com.extfro.extfrocore.api.machine.feature.IRecipeLogicMachine;
import com.extfro.extfrocore.api.recipe.ActionResult;
import com.extfro.extfrocore.api.recipe.MachineRecipe;
import com.extfro.extfrocore.api.recipe.RecipeHelper;
import com.extfro.extfrocore.api.recipe.RecipeLogicContext;
import com.extfro.extfrocore.api.registry.EFRegistries;
import com.extfro.extfrocore.api.sync_system.ISyncManaged;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.sync_system.holder.SyncDataHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RecipeLogic implements RecipeLogicContext, ISyncManaged {

    protected static class ChanceCacheMap extends IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> {}

    public enum Status implements StringRepresentable {

        IDLE("idle"),
        WORKING("working"),
        WAITING("waiting"),
        SUSPEND("suspend");

        @Getter
        private final String serializedName;

        Status(String serializedName) {
            this.serializedName = serializedName;
        }
    }

    public final IRecipeLogicMachine machine;
    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);
    public @Nullable List<MachineRecipe> lastFailedMatches;

    @Getter
    @SaveField
    @SyncToClient
    private Status status = Status.IDLE;

    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean isActive;

    @Getter
    @Nullable
    @SaveField
    @SyncToClient
    private Component waitingReason = null;

    @SyncToClient
    protected final List<Component> failureReasons = new ArrayList<>();
    @Getter
    protected final Map<MachineRecipe, Component> failureReasonMap = new HashMap<>();

    @Getter
    protected MachineRecipe lastRecipe;
    @Getter
    @SaveField
    @SyncToClient
    protected int consecutiveRecipes = 0;
    @Getter
    protected MachineRecipe lastOriginRecipe;
    @SaveField
    @Getter
    @SyncToClient
    protected int progress;
    @Getter
    @SyncToClient
    @SaveField
    protected int duration;
    @Getter(onMethod_ = @VisibleForTesting)
    protected boolean recipeDirty;
    @Getter
    protected long totalContinuousRunningTime;
    protected int runAttempt = 0;
    protected int runDelay = 0;
    @SaveField
    @Getter
    @Setter
    protected boolean suspendAfterFinish = false;
    @Getter
    protected final ChanceCacheMap chanceCaches = makeChanceCaches();
    protected @Nullable TickableSubscription subscription;

    public RecipeLogic(IRecipeLogicMachine machine) {
        this.machine = machine;
    }

    @Override
    public @Nullable MetaMachine machine() {
        return machine.self();
    }

    @Override
    public int recipeTier() {
        return machine.self().getDefinition().getTier();
    }

    public MetaMachine getMachine() {
        return machine.self();
    }

    @Override
    public void scheduleRenderUpdate() {
        getMachine().scheduleRenderUpdate();
    }

    @Override
    public void markAsChanged() {
        getMachine().markAsChanged();
    }

    public void resetRecipeLogic() {
        recipeDirty = false;
        lastRecipe = null;
        lastOriginRecipe = null;
        consecutiveRecipes = 0;
        progress = 0;
        duration = 0;
        isActive = false;
        lastFailedMatches = null;
        waitingReason = null;
        failureReasons.clear();
        if (status != Status.SUSPEND) {
            setStatus(Status.IDLE);
        }
        updateTickSubscription();
        getMachine().getSyncDataHolder().resyncAllFields();
    }

    public void onMachineLoad() {
        updateTickSubscription();
    }

    public void updateTickSubscription() {
        if (isSuspend() || !machine.isRecipeLogicAvailable()) {
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        } else {
            subscription = getMachine().subscribeServerTick(subscription, this::serverTick);
        }
    }

    public void setProgress(int progress) {
        this.progress = progress;
        syncDataHolder.resyncAllFields();
        getMachine().getSyncDataHolder().resyncAllFields();
    }

    public double getProgressPercent() {
        return duration == 0 ? 0.0 : progress / (duration * 1.0);
    }

    public void serverTick() {
        if (!isSuspend()) {
            if (!isIdle() && lastRecipe != null) {
                if (progress < duration) {
                    if (runDelay > 0) {
                        runDelay--;
                    } else {
                        handleRecipeWorking();
                    }
                }
                if (progress >= duration) {
                    onRecipeFinish();
                }
            } else if (lastRecipe != null) {
                findAndHandleRecipe();
            } else if (!machine.keepSubscribing() || getMachine().getOffsetTimer() % 5 == 0) {
                findAndHandleRecipe();
                if (lastFailedMatches != null) {
                    for (MachineRecipe match : lastFailedMatches) {
                        if (checkMatchedRecipeAvailable(match)) break;
                    }
                }
            }
        }

        boolean unsubscribe = false;
        if (isSuspend()) {
            unsubscribe = true;
        } else if (lastRecipe == null && isIdle() && !machine.keepSubscribing() && !recipeDirty &&
                lastFailedMatches == null) {
                    unsubscribe = true;
                }
        if (isIdle()) {
            failureReasons.clear();
            failureReasons.addAll(failureReasonMap.values());
        }
        if (unsubscribe && subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    protected ActionResult matchRecipe(MachineRecipe recipe) {
        return RecipeHelper.matchContents(machine, recipe);
    }

    protected ActionResult checkRecipe(MachineRecipe recipe) {
        ActionResult conditionResult = RecipeHelper.checkConditions(recipe, this);
        if (!conditionResult.isSuccess()) return conditionResult;
        return matchRecipe(recipe);
    }

    public boolean checkMatchedRecipeAvailable(MachineRecipe match) {
        MachineRecipe modified = machine.fullModifyRecipe(match);
        if (modified != null) {
            ActionResult recipeMatch = checkRecipe(modified);
            if (recipeMatch.isSuccess()) {
                setupRecipe(modified);
            } else {
                putFailureReason(this, match, recipeMatch.reason());
            }
            if (lastRecipe != null && getStatus() == Status.WORKING) {
                lastOriginRecipe = match;
                lastFailedMatches = null;
                return true;
            }
        }
        return false;
    }

    public void handleRecipeWorking() {
        assert lastRecipe != null;
        ActionResult conditionResult = RecipeHelper.checkConditions(lastRecipe, this);
        if (conditionResult.isSuccess()) {
            ActionResult handleTick = handleTickRecipe(lastRecipe);
            if (handleTick.isSuccess()) {
                setStatus(Status.WORKING);
                if (!machine.onWorking()) {
                    interruptRecipe();
                    return;
                }
                progress++;
                totalContinuousRunningTime++;
                runAttempt = 0;
                runDelay = 0;
            } else {
                setWaiting(handleTick.reason());
                runAttempt = Math.min(runAttempt + 1, 5);
                runDelay = runAttempt * 60;
            }
        } else {
            setWaiting(conditionResult.reason());
        }
        if (isWaiting() || isSuspend()) {
            regressRecipe();
        }
    }

    protected void regressRecipe() {
        if (progress > 0 && machine.regressWhenWaiting()) {
            progress = 1;
        }
    }

    public Iterator<MachineRecipe> searchRecipe() {
        return machine.getRecipeType().searchRecipe(this, recipe -> true);
    }

    public void findAndHandleRecipe() {
        lastFailedMatches = null;

        if (!recipeDirty && lastRecipe != null && checkRecipe(lastRecipe).isSuccess()) {
            MachineRecipe recipe = lastRecipe;
            lastRecipe = null;
            lastOriginRecipe = null;
            setupRecipe(recipe);
        } else {
            failureReasonMap.clear();
            lastRecipe = null;
            lastOriginRecipe = null;
            handleSearchingRecipes(searchRecipe());
        }
        recipeDirty = false;
    }

    protected void handleSearchingRecipes(Iterator<MachineRecipe> matches) {
        while (matches.hasNext()) {
            MachineRecipe match = matches.next();
            if (checkMatchedRecipeAvailable(match)) return;

            if (!matchRecipe(match).isSuccess()) {
                continue;
            }

            if (lastFailedMatches == null) {
                lastFailedMatches = new ArrayList<>();
            }
            lastFailedMatches.add(match);
        }
    }

    public ActionResult handleTickRecipe(MachineRecipe recipe) {
        if (!recipe.hasTick()) return ActionResult.SUCCESS;

        ActionResult result = RecipeHelper.matchTickRecipe(machine, recipe);
        if (!result.isSuccess()) return result;

        result = handleTickRecipeIO(recipe, IO.IN);
        if (!result.isSuccess()) return result;

        return handleTickRecipeIO(recipe, IO.OUT);
    }

    public void setupRecipe(MachineRecipe recipe) {
        if (!machine.beforeWorking(recipe)) {
            setStatus(Status.IDLE);
            consecutiveRecipes = 0;
            progress = 0;
            duration = 0;
            isActive = false;
            return;
        }
        ActionResult handledIO = handleRecipeIO(recipe, IO.IN);
        if (handledIO.isSuccess()) {
            if (lastRecipe != null && !recipe.equals(lastRecipe)) {
                chanceCaches.clear();
            }
            failureReasonMap.clear();
            recipeDirty = false;
            lastRecipe = recipe;
            setStatus(Status.WORKING);
            progress = 0;
            duration = recipe.duration;
            isActive = true;
        }
    }

    public void setStatus(Status status) {
        if (this.status != status) {
            if (this.status == Status.WORKING) {
                totalContinuousRunningTime = 0;
            }
            if ((status == Status.WAITING || status == Status.SUSPEND) && suspendAfterFinish) {
                status = Status.SUSPEND;
                suspendAfterFinish = false;
            }
            machine.notifyStatusChanged(this.status, status);
            this.status = status;
            syncDataHolder.resyncAllFields();
            getMachine().getSyncDataHolder().resyncAllFields();
            getMachine().scheduleRenderUpdate();
            updateTickSubscription();
            if (this.status != Status.WAITING) {
                waitingReason = null;
            }
        }
    }

    public void setWaiting(@Nullable Component reason) {
        setStatus(Status.WAITING);
        waitingReason = reason;
        machine.onWaiting();
    }

    public void markLastRecipeDirty() {
        recipeDirty = true;
    }

    public boolean isWorking() {
        return status == Status.WORKING;
    }

    public boolean isIdle() {
        return status == Status.IDLE;
    }

    public boolean isWaiting() {
        return status == Status.WAITING;
    }

    public boolean isSuspend() {
        return status == Status.SUSPEND;
    }

    public boolean isWorkingEnabled() {
        return !isSuspend() && !isSuspendAfterFinish();
    }

    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (!isWorkingAllowed && getStatus() == Status.IDLE) {
            setStatus(Status.SUSPEND);
        } else {
            setSuspendAfterFinish(!isWorkingAllowed);
            if (isWorkingAllowed) {
                if (lastRecipe != null && duration > 0) {
                    setStatus(Status.WORKING);
                } else {
                    setStatus(Status.IDLE);
                }
            }
        }
    }

    public int getMaxProgress() {
        return duration;
    }

    public boolean isActive() {
        return isWorking() || isWaiting() || (isSuspend() && isActive);
    }

    public boolean hasCustomProgressLine() {
        return false;
    }

    public @Nullable Component getCustomProgressLine() {
        return null;
    }

    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe != null) {
            runAttempt = 0;
            runDelay = 0;
            consecutiveRecipes++;
            handleRecipeIO(lastRecipe, IO.OUT);
            if (suspendAfterFinish) {
                setStatus(Status.SUSPEND);
                consecutiveRecipes = 0;
                progress = 0;
                duration = 0;
                isActive = false;
                lastRecipe = null;
                return;
            }
            if (machine.alwaysTryModifyRecipe()) {
                if (lastOriginRecipe != null) {
                    MachineRecipe modified = machine.fullModifyRecipe(lastOriginRecipe.copy());
                    if (modified == null) {
                        markLastRecipeDirty();
                    } else {
                        lastRecipe = modified;
                    }
                } else {
                    markLastRecipeDirty();
                }
            }
            ActionResult recipeCheck = checkRecipe(lastRecipe);
            if (!recipeDirty && recipeCheck.isSuccess()) {
                setupRecipe(lastRecipe);
            } else {
                setStatus(Status.IDLE);
                consecutiveRecipes = 0;
                progress = 0;
                duration = 0;
                isActive = false;
            }
        }
    }

    protected ActionResult handleRecipeIO(MachineRecipe recipe, IO io) {
        return RecipeHelper.handleRecipeIO(machine, recipe, io, chanceCaches);
    }

    protected ActionResult handleTickRecipeIO(MachineRecipe recipe, IO io) {
        return RecipeHelper.handleTickRecipeIO(machine, recipe, io, chanceCaches);
    }

    public void interruptRecipe() {
        machine.afterWorking();
        if (lastRecipe != null) {
            setStatus(Status.IDLE);
            progress = 0;
            duration = 0;
        }
    }

    protected ChanceCacheMap makeChanceCaches() {
        ChanceCacheMap map = new ChanceCacheMap();
        for (RecipeCapability<?> capability : EFRegistries.RECIPE_CAPABILITIES) {
            map.put(capability, capability.makeChanceCache());
        }
        return map;
    }

    public static void putFailureReason(Object machine, MachineRecipe recipe, Component reason) {
        if (machine instanceof IRecipeLogicMachine logicMachine) {
            putFailureReason(logicMachine.getRecipeLogic(), recipe, reason);
        }
    }

    public static void putFailureReason(RecipeLogic logic, MachineRecipe recipe, Component reason) {
        Map<MachineRecipe, Component> map = logic.getFailureReasonMap();
        if (map.containsKey(recipe)) {
            map.put(recipe, reason);
        } else {
            map.put(recipe, reason);
        }
    }
}
