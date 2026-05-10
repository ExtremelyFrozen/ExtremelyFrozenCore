package com.extfro.extfrocore.api.sync_system;

import com.extfro.extfrocore.api.sync_system.holder.SyncDataHolder;

/**
 * Represents a class with fields that have sync annotations.
 * <p>
 * A field can be marked with sync annotations if the field type has a registered codec or implements
 * {@link ISyncManaged}.
 *
 * @see SyncDataHolder
 */
public interface ISyncManaged {

    SyncDataHolder getSyncDataHolder();

    /**
     * Function called when a synced field requests a rerender
     */
    void scheduleRenderUpdate();

    /**
     * Function called to notify the server that this object has been updated and must be synced to clients
     */
    void markAsChanged();
}
