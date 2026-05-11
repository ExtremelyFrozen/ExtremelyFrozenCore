package com.extfro.extfrocore.api.recipe;

import com.extfro.extfrocore.api.recipe.condition.RecipeConditionType;
import com.extfro.extfrocore.api.registry.EFRegistries;

import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;

import com.google.gson.JsonObject;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Accessors(chain = true)
public abstract class RecipeCondition<T extends RecipeCondition<T>> {

    public static final Codec<RecipeCondition<?>> CODEC = EFRegistries.RECIPE_CONDITIONS.byNameCodec()
            .dispatch(RecipeCondition::getType, RecipeConditionType::getCodec);

    public static <RC extends RecipeCondition<RC>> Products.P1<RecordCodecBuilder.Mu<RC>, Boolean> isReverse(
                                                                                                             RecordCodecBuilder.Instance<RC> instance) {
        return instance.group(Codec.BOOL.optionalFieldOf("reverse", false).forGetter(value -> value.isReverse));
    }

    public static <RC extends RecipeCondition<RC>> MapCodec<RC> simpleCodec(Function<Boolean, RC> function) {
        return RecordCodecBuilder.mapCodec(instance -> isReverse(instance).apply(instance, function));
    }

    @Getter
    @Setter
    protected boolean isReverse;

    public RecipeCondition() {
        this(false);
    }

    public RecipeCondition(boolean isReverse) {
        this.isReverse = isReverse;
    }

    public abstract RecipeConditionType<T> getType();

    public String getTranslationKey() {
        return "extfrocore.recipe.condition." + EFRegistries.RECIPE_CONDITIONS.getKey(getType()).getPath();
    }

    public boolean isOr() {
        return false;
    }

    public abstract Component getTooltips();

    public boolean check(@NotNull MachineRecipe recipe, @NotNull RecipeLogicContext context) {
        boolean test = testCondition(recipe, context);
        return test != isReverse;
    }

    protected abstract boolean testCondition(@NotNull MachineRecipe recipe, @NotNull RecipeLogicContext context);

    public abstract T createTemplate();

    @NotNull
    public final JsonObject serialize() {
        var ops = RegistryOps.create(JsonOps.INSTANCE, EFRegistries.builtinRegistry());
        return CODEC.encodeStart(ops, this).getOrThrow().getAsJsonObject();
    }

    public static RecipeCondition<?> deserialize(@NotNull JsonObject config) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, EFRegistries.builtinRegistry());
        return CODEC.decode(ops, config).getOrThrow().getFirst();
    }

    public final void toNetwork(RegistryFriendlyByteBuf buf) {
        var ops = RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess());
        buf.writeWithCodec(ops, CODEC, this);
    }

    public static RecipeCondition<?> fromNetwork(RegistryFriendlyByteBuf buf) {
        var ops = RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess());
        return buf.readWithCodec(ops, CODEC, NbtAccounter.create(FriendlyByteBuf.DEFAULT_NBT_QUOTA));
    }
}
