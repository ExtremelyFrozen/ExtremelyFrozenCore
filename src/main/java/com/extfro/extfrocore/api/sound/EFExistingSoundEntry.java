package com.extfro.extfrocore.api.sound;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class EFExistingSoundEntry extends EFSoundEntry {

    protected final SoundEvent event;

    public EFExistingSoundEntry(SoundEvent event, SoundSource category) {
        super(event.getLocation(), null, category, 0);
        this.event = event;
    }

    @Override
    public void prepare() {}

    @Override
    public void register(Consumer<SoundEvent> registry) {}

    @Override
    public void write(JsonObject json) {}

    @Override
    public SoundEvent getMainEvent() {
        return event;
    }

    @Override
    public void play(Level level, Player player, double x, double y, double z, float volume, float pitch) {
        level.playSound(player, x, y, z, event, category, volume, pitch);
    }

    @Override
    public void playAt(Level level, double x, double y, double z, float volume, float pitch, boolean fade) {
        level.playLocalSound(x, y, z, event, category, volume, pitch, fade);
    }
}
