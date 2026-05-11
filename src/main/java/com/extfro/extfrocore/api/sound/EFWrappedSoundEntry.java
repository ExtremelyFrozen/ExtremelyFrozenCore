package com.extfro.extfrocore.api.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EFWrappedSoundEntry extends EFSoundEntry {

    private final List<EFConfiguredSoundEvent> wrappedEvents;
    private final List<CompiledSoundEvent> compiledEvents;

    public EFWrappedSoundEntry(ResourceLocation id, String subtitle,
                               List<EFConfiguredSoundEvent> wrappedEvents,
                               SoundSource category, int attenuationDistance) {
        super(id, subtitle, category, attenuationDistance);
        this.wrappedEvents = wrappedEvents;
        compiledEvents = new ArrayList<>();
    }

    @Override
    public void prepare() {
        for (int i = 0; i < wrappedEvents.size(); i++) {
            EFConfiguredSoundEvent wrapped = wrappedEvents.get(i);
            ResourceLocation location = getIdOf(i);
            compiledEvents.add(new CompiledSoundEvent(SoundEvent.createVariableRangeEvent(location),
                    wrapped.volume(), wrapped.pitch()));
        }
    }

    @Override
    public void register(Consumer<SoundEvent> registry) {
        for (CompiledSoundEvent compiledEvent : compiledEvents) {
            registry.accept(compiledEvent.event());
        }
    }

    @Override
    public SoundEvent getMainEvent() {
        return compiledEvents.getFirst().event();
    }

    protected ResourceLocation getIdOf(int index) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                index == 0 ? id.getPath() : id.getPath() + "_wrapped_" + index);
    }

    @Override
    public void write(JsonObject json) {
        for (int i = 0; i < wrappedEvents.size(); i++) {
            EFConfiguredSoundEvent event = wrappedEvents.get(i);
            JsonObject entry = new JsonObject();
            JsonArray list = new JsonArray();
            JsonObject sound = new JsonObject();
            sound.addProperty("name", event.event().get().getLocation().toString());
            sound.addProperty("type", "event");
            if (attenuationDistance != 0) {
                sound.addProperty("attenuation_distance", attenuationDistance);
            }
            list.add(sound);
            entry.add("sounds", list);
            if (i == 0 && hasSubtitle()) {
                entry.addProperty("subtitle", getSubtitleKey());
            }
            json.add(getIdOf(i).getPath(), entry);
        }
    }

    @Override
    public void play(Level level, Player player, double x, double y, double z, float volume, float pitch) {
        for (CompiledSoundEvent event : compiledEvents) {
            level.playSound(player, x, y, z, event.event(), category,
                    event.volume() * volume, event.pitch() * pitch);
        }
    }

    @Override
    public void playAt(Level level, double x, double y, double z, float volume, float pitch, boolean fade) {
        for (CompiledSoundEvent event : compiledEvents) {
            level.playLocalSound(x, y, z, event.event(), category,
                    event.volume() * volume, event.pitch() * pitch, fade);
        }
    }

    private record CompiledSoundEvent(SoundEvent event, float volume, float pitch) {}
}
