package com.extfro.extfrocore.api.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.function.Consumer;

public class EFCustomSoundEntry extends EFSoundEntry {

    protected final List<ResourceLocation> variants;
    protected SoundEvent event;

    public EFCustomSoundEntry(ResourceLocation id, List<ResourceLocation> variants, String subtitle,
                              SoundSource category, int attenuationDistance) {
        super(id, subtitle, category, attenuationDistance);
        this.variants = variants;
    }

    @Override
    public void prepare() {
        event = SoundEvent.createVariableRangeEvent(id);
    }

    @Override
    public void register(Consumer<SoundEvent> registry) {
        registry.accept(event);
    }

    @Override
    public SoundEvent getMainEvent() {
        return event;
    }

    @Override
    public void write(JsonObject json) {
        JsonObject entry = new JsonObject();
        JsonArray list = new JsonArray();

        JsonObject sound = new JsonObject();
        sound.addProperty("name", id.toString());
        sound.addProperty("type", "file");
        if (attenuationDistance != 0) {
            sound.addProperty("attenuation_distance", attenuationDistance);
        }
        list.add(sound);

        for (ResourceLocation variant : variants) {
            sound = new JsonObject();
            sound.addProperty("name", variant.toString());
            sound.addProperty("type", "file");
            if (attenuationDistance != 0) {
                sound.addProperty("attenuation_distance", attenuationDistance);
            }
            list.add(sound);
        }

        entry.add("sounds", list);
        if (hasSubtitle()) {
            entry.addProperty("subtitle", getSubtitleKey());
        }
        json.add(id.getPath(), entry);
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
