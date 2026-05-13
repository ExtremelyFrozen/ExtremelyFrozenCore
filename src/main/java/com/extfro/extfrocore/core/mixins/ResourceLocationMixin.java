package com.extfro.extfrocore.core.mixins;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.core.IResourceLocationExtensions;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResourceLocation.class)
public class ResourceLocationMixin implements IResourceLocationExtensions {

    @Unique
    private boolean gtm$isImplicit;

    @Override
    public boolean gtm$getImplicit() {
        return this.gtm$isImplicit;
    }

    @Override
    public void gtm$setImplicit(boolean implicit) {
        this.gtm$isImplicit = implicit;
    }

    @Override
    public ResourceLocation gtm$asNonImplicit() {
        if (this.gtm$getImplicit()) {
            return ExtForCore.id(((ResourceLocation) (Object) this).getPath());
        }
        return (ResourceLocation) (Object) this;
    }

    @Inject(method = "<init>*", at = @At("TAIL"))
    private void populateImplicit(String namespace, String path, CallbackInfo info) {
        gtm$isImplicit = false;
    }
}
