package com.extfro.extfrocore.common.cover.detector;

import com.extfro.extfrocore.api.capability.IControllable;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.machine.TickableSubscription;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.common.data.item.GTItemAbilities;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

import lombok.Getter;
import lombok.Setter;

public abstract class DetectorCover extends CoverBehavior implements IControllable {

    @SaveField
    @Getter
    @Setter
    protected boolean isWorkingEnabled = true;
    protected TickableSubscription subscription;
    private boolean forceUpdate;

    @SaveField
    @SyncToClient
    @Getter
    private boolean isInverted;

    public DetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        forceUpdate();
        subscription = coverHolder.subscribeServerTick(subscription, this::update);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public void setInverted(boolean inverted) {
        isInverted = inverted;
        syncDataHolder.markClientSyncFieldDirty("isInverted");
    }

    protected boolean shouldUpdate() {
        return forceUpdate || this.coverHolder.getOffsetTimer() % 20 == 0;
    }

    protected void forceUpdate() {
        forceUpdate = true;
        try {
            update();
        } finally {
            forceUpdate = false;
        }
    }

    protected abstract void update();

    private void toggleInvertedWithNotification() {
        setInverted(!isInverted());

        if (!this.coverHolder.isRemote()) {
            this.coverHolder.notifyBlockUpdate();
        }
    }

    @Override
    public InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        InteractionResult superResult = super.onScrewdriverClick(context);
        if (superResult != InteractionResult.PASS) {
            return superResult;
        }
        if (!context.getItemInHand().canPerformAction(GTItemAbilities.SCREWDRIVER_CONFIGURE)) {
            return InteractionResult.FAIL;
        }

        if (!this.coverHolder.isRemote()) {
            toggleInvertedWithNotification();

            String translationKey = isInverted() ? "cover.detector_base.message_inverted_state" :
                    "cover.detector_base.message_normal_state";
            context.getPlayer().sendSystemMessage(Component.translatable(translationKey));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public boolean canPipePassThrough() {
        return false;
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putBoolean("inverted", isInverted);
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setInverted(tag.getBoolean("inverted"));
        super.pasteConfig(player, tag);
    }
}
