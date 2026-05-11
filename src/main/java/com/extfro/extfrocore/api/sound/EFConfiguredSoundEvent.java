package com.extfro.extfrocore.api.sound;

import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public record EFConfiguredSoundEvent(Supplier<SoundEvent> event, float volume, float pitch) {}
