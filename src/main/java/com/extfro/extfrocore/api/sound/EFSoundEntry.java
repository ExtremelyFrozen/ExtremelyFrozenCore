package com.extfro.extfrocore.api.sound;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.google.gson.JsonObject;

import java.util.function.Consumer;

public abstract class EFSoundEntry {

    protected final ResourceLocation id;
    protected final String subtitle;
    protected final SoundSource category;
    protected final int attenuationDistance;

    public EFSoundEntry(ResourceLocation id, String subtitle, SoundSource category, int attenuationDistance) {
        this.id = id;
        this.subtitle = subtitle;
        this.category = category;
        this.attenuationDistance = attenuationDistance;
    }

    public abstract void prepare();

    public abstract void register(Consumer<SoundEvent> registry);

    public abstract void write(JsonObject json);

    public abstract SoundEvent getMainEvent();

    public String getSubtitleKey() {
        return id.getNamespace() + ".subtitle." + id.getPath();
    }

    public ResourceLocation getId() {
        return id;
    }

    public boolean hasSubtitle() {
        return subtitle != null;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void playOnServer(Level level, Vec3i pos) {
        playOnServer(level, pos, 1, 1);
    }

    public void playOnServer(Level level, Vec3i pos, float volume, float pitch) {
        play(level, null, pos, volume, pitch);
    }

    public void play(Level level, Player player, Vec3i pos) {
        play(level, player, pos, 1, 1);
    }

    public void playFrom(Entity entity) {
        playFrom(entity, 1, 1);
    }

    public void playFrom(Entity entity, float volume, float pitch) {
        if (!entity.isSilent()) {
            play(entity.level(), null, entity.blockPosition(), volume, pitch);
        }
    }

    public void play(Level level, Player player, Vec3i pos, float volume, float pitch) {
        play(level, player, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, volume, pitch);
    }

    public void play(Level level, Player player, Vec3 pos, float volume, float pitch) {
        play(level, player, pos.x(), pos.y(), pos.z(), volume, pitch);
    }

    public abstract void play(Level level, Player player, double x, double y, double z, float volume, float pitch);

    public void playAt(Level level, Vec3i pos, float volume, float pitch, boolean fade) {
        playAt(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, volume, pitch, fade);
    }

    public void playAt(Level level, Vec3 pos, float volume, float pitch, boolean fade) {
        playAt(level, pos.x(), pos.y(), pos.z(), volume, pitch, fade);
    }

    public abstract void playAt(Level level, double x, double y, double z, float volume, float pitch, boolean fade);
}
