package com.extfro.extfrocore.api.fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;

public interface EFPropertyFluidFilter {

    boolean canContain(@NotNull EFFluidState state);

    boolean canContain(@NotNull EFFluidAttribute attribute);

    void setCanContain(@NotNull EFFluidAttribute attribute, boolean canContain);

    @NotNull
    @UnmodifiableView
    Collection<@NotNull EFFluidAttribute> getContainedAttributes();
}
