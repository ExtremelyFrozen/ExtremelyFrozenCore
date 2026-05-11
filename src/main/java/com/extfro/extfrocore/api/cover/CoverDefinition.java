package com.extfro.extfrocore.api.cover;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.client.renderer.cover.ICoverRenderer;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class CoverDefinition {

    private static final Map<net.minecraft.world.item.Item, CoverDefinition> ITEM_LOOKUP = new ConcurrentHashMap<>();

    public interface CoverBehaviourProvider {

        CoverBehavior create(CoverDefinition definition, ICoverable coverable, Direction side);
    }

    public interface TieredCoverBehaviourProvider {

        CoverBehavior create(CoverDefinition definition, ICoverable coverable, Direction side, int tier);
    }

    @Getter
    private final ResourceLocation id;
    private final CoverBehaviourProvider behaviorCreator;
    @Getter
    private final @Nullable Supplier<ICoverRenderer> coverRenderer;

    public CoverDefinition(ResourceLocation id, CoverBehaviourProvider behaviorCreator,
                           Supplier<Supplier<ICoverRenderer>> coverRenderer) {
        this.id = id;
        this.behaviorCreator = behaviorCreator;
        if (ExtForCore.isClientSide()) {
            this.coverRenderer = ClientHelper.initRenderer(coverRenderer);
        } else {
            this.coverRenderer = null;
        }
    }

    public CoverBehavior createCoverBehavior(ICoverable coverable, Direction side) {
        return behaviorCreator.create(this, coverable, side);
    }

    public void bindItem(net.minecraft.world.item.Item item) {
        ITEM_LOOKUP.put(item, this);
    }

    public void bindItem(Supplier<? extends net.minecraft.world.item.Item> item) {
        bindItem(item.get());
    }

    public static Optional<CoverDefinition> getForItem(net.minecraft.world.item.ItemStack stack) {
        return Optional.ofNullable(ITEM_LOOKUP.get(stack.getItem()));
    }

    private static class ClientHelper {

        private static Supplier<ICoverRenderer> initRenderer(Supplier<Supplier<ICoverRenderer>> coverRenderer) {
            ICoverRenderer value = coverRenderer.get().get();
            return () -> value;
        }
    }
}
