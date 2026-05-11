package com.extfro.extfrocore.api.data.worldgen.ores;

import com.extfro.extfrocore.api.data.worldgen.OreDefinition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
public final class GeneratedVeinMetadata {

    public static final Codec<ChunkPos> CHUNK_POS_CODEC = Codec.LONG.xmap(ChunkPos::new, ChunkPos::toLong);
    public static final Codec<GeneratedVeinMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CHUNK_POS_CODEC.fieldOf("origin_chunk").forGetter(GeneratedVeinMetadata::originChunk),
            BlockPos.CODEC.fieldOf("center").forGetter(GeneratedVeinMetadata::center),
            OreDefinition.CODEC.fieldOf("definition").forGetter(GeneratedVeinMetadata::definition),
            Codec.BOOL.optionalFieldOf("depleted", false).forGetter(GeneratedVeinMetadata::depleted))
            .apply(instance, GeneratedVeinMetadata::new));

    @Getter
    @NotNull
    private final ChunkPos originChunk;
    @Getter
    @NotNull
    private final BlockPos center;
    @Getter
    @Setter
    @NotNull
    private Holder<OreDefinition> definition;
    @Getter
    @Setter
    private boolean depleted;

    public GeneratedVeinMetadata(@NotNull ChunkPos originChunk, @NotNull BlockPos center,
                                 @NotNull Holder<OreDefinition> definition) {
        this(originChunk, center, definition, false);
    }

    public GeneratedVeinMetadata(@NotNull ChunkPos originChunk, @NotNull BlockPos center,
                                 @NotNull Holder<OreDefinition> definition, boolean depleted) {
        this.originChunk = originChunk;
        this.center = center;
        this.definition = definition;
        this.depleted = depleted;
    }

    public static GeneratedVeinMetadata readFromPacket(RegistryFriendlyByteBuf buf) {
        ChunkPos origin = new ChunkPos(buf.readVarLong());
        BlockPos center = BlockPos.of(buf.readVarLong());
        Holder<OreDefinition> definition = OreDefinition.STREAM_CODEC.decode(buf);
        return new GeneratedVeinMetadata(origin, center, definition, false);
    }

    public void writeToPacket(RegistryFriendlyByteBuf buf) {
        buf.writeVarLong(originChunk.toLong());
        buf.writeVarLong(center.asLong());
        OreDefinition.STREAM_CODEC.encode(buf, definition);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GeneratedVeinMetadata that)) return false;
        return originChunk.equals(that.originChunk) && center.equals(that.center) && definition == that.definition;
    }

    @Override
    public int hashCode() {
        int result = originChunk.hashCode();
        result = 31 * result + center.hashCode();
        result = 31 * result + definition.hashCode();
        return result;
    }
}
