package com.extfro.extfrocore.api.recipe;

import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.recipe.category.RecipeCategory;
import com.extfro.extfrocore.api.recipe.chance.logic.ChanceLogic;
import com.extfro.extfrocore.api.recipe.content.Content;
import com.extfro.extfrocore.api.registry.EFRegistries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class MachineRecipeSerializer implements RecipeSerializer<MachineRecipe> {

    public static final Codec<MachineRecipeType> MACHINE_RECIPE_TYPE_CODEC = BuiltInRegistries.RECIPE_TYPE.byNameCodec()
            .comapFlatMap(recipeType -> {
                if (recipeType instanceof MachineRecipeType machineRecipeType) {
                    return DataResult.success(machineRecipeType);
                }
                return DataResult.error(() -> "Recipe type " + recipeType + " is not a MachineRecipeType");
            }, Function.identity());

    public static final StreamCodec<ByteBuf, MachineRecipeType> MACHINE_RECIPE_TYPE_STREAM_CODEC = new StreamCodec<>() {

        private static final StreamCodec<ByteBuf, RecipeType<?>> STREAM_CODEC = ResourceLocation.STREAM_CODEC
                .map(BuiltInRegistries.RECIPE_TYPE::get, BuiltInRegistries.RECIPE_TYPE::getKey);

        @Override
        public @NotNull MachineRecipeType decode(@NotNull ByteBuf buffer) {
            RecipeType<?> recipeType = STREAM_CODEC.decode(buffer);
            if (!(recipeType instanceof MachineRecipeType machineRecipeType)) {
                throw new DecoderException("Recipe type " + recipeType + " is not a MachineRecipeType");
            }
            return machineRecipeType;
        }

        @Override
        public void encode(@NotNull ByteBuf buffer, @NotNull MachineRecipeType value) {
            STREAM_CODEC.encode(buffer, value);
        }
    };

    public static final Codec<Map<RecipeCapability<?>, ChanceLogic>> CHANCE_LOGIC_MAP_CODEC =
            Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, EFRegistries.CHANCE_LOGICS.byNameCodec());

    public static final MapCodec<MachineRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    MACHINE_RECIPE_TYPE_CODEC.fieldOf("type").forGetter(value -> value.recipeType),
                    RecipeCapability.CODEC.optionalFieldOf("inputs", Map.of()).forGetter(value -> value.inputs),
                    RecipeCapability.CODEC.optionalFieldOf("outputs", Map.of()).forGetter(value -> value.outputs),
                    RecipeCapability.CODEC.optionalFieldOf("tickInputs", Map.of()).forGetter(value -> value.tickInputs),
                    RecipeCapability.CODEC.optionalFieldOf("tickOutputs", Map.of()).forGetter(value -> value.tickOutputs),
                    CHANCE_LOGIC_MAP_CODEC.optionalFieldOf("inputChanceLogics", Map.of()).forGetter(value -> value.inputChanceLogics),
                    CHANCE_LOGIC_MAP_CODEC.optionalFieldOf("outputChanceLogics", Map.of()).forGetter(value -> value.outputChanceLogics),
                    CHANCE_LOGIC_MAP_CODEC.optionalFieldOf("tickInputChanceLogics", Map.of()).forGetter(value -> value.tickInputChanceLogics),
                    CHANCE_LOGIC_MAP_CODEC.optionalFieldOf("tickOutputChanceLogics", Map.of()).forGetter(value -> value.tickOutputChanceLogics),
                    RecipeCondition.CODEC.listOf().optionalFieldOf("recipeConditions", List.of()).forGetter(value -> value.conditions),
                    RecipeDataMap.CODEC.optionalFieldOf("data", RecipeDataMap.empty()).forGetter(value -> value.data),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("duration").forGetter(value -> value.duration),
                    EFRegistries.RECIPE_CATEGORIES.byNameCodec().optionalFieldOf("category", RecipeCategory.DEFAULT).forGetter(value -> value.recipeCategory),
                    Codec.INT.optionalFieldOf("groupColor", -1).forGetter(value -> value.groupColor))
            .apply(instance, MachineRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> STREAM_CODEC =
            StreamCodec.of(MachineRecipeSerializer::toNetwork, MachineRecipeSerializer::fromNetwork);

    private MachineRecipeSerializer() {}

    public static RecipeSerializer<MachineRecipe> create() {
        return new MachineRecipeSerializer();
    }

    @Override
    public @NotNull MapCodec<MachineRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    public static Tuple<RecipeCapability<?>, List<Content>> entryReader(RegistryFriendlyByteBuf buf) {
        RecipeCapability<?> capability = EFRegistries.RECIPE_CAPABILITIES.get(buf.readResourceLocation());
        List<Content> contents = readCollection(buf, capability.serializer::fromNetworkContent);
        return new Tuple<>(capability, contents);
    }

    public static Tuple<RecipeCapability<?>, ChanceLogic> chanceLogicEntryReader(RegistryFriendlyByteBuf buf) {
        RecipeCapability<?> capability = EFRegistries.RECIPE_CAPABILITIES.get(buf.readResourceLocation());
        ChanceLogic logic = EFRegistries.CHANCE_LOGICS.get(buf.readResourceLocation());
        return new Tuple<>(capability, logic);
    }

    public static void entryWriter(RegistryFriendlyByteBuf buf,
                                   Map.Entry<RecipeCapability<?>, ? extends List<Content>> entry) {
        RecipeCapability<?> capability = entry.getKey();
        buf.writeResourceLocation(EFRegistries.RECIPE_CAPABILITIES.getKey(capability));
        writeCollection(entry.getValue(), buf, capability.serializer::toNetworkContent);
    }

    public static void chanceLogicEntryWriter(RegistryFriendlyByteBuf buf,
                                              Map.Entry<RecipeCapability<?>, ChanceLogic> entry) {
        buf.writeResourceLocation(EFRegistries.RECIPE_CAPABILITIES.getKey(entry.getKey()));
        buf.writeResourceLocation(EFRegistries.CHANCE_LOGICS.getKey(entry.getValue()));
    }

    public static Map<RecipeCapability<?>, List<Content>> tuplesToMap(List<Tuple<RecipeCapability<?>, List<Content>>> entries) {
        Map<RecipeCapability<?>, List<Content>> map = new HashMap<>();
        entries.forEach(entry -> map.put(entry.getA(), entry.getB()));
        return map;
    }

    public static Map<RecipeCapability<?>, ChanceLogic> logicTuplesToMap(List<Tuple<RecipeCapability<?>, ChanceLogic>> entries) {
        Map<RecipeCapability<?>, ChanceLogic> map = new HashMap<>();
        entries.forEach(entry -> map.put(entry.getA(), entry.getB()));
        return map;
    }

    @NotNull
    public static MachineRecipe fromNetwork(@NotNull RegistryFriendlyByteBuf buf) {
        ResourceLocation recipeType = buf.readResourceLocation();
        ResourceLocation id = buf.readResourceLocation();
        int duration = buf.readVarInt();
        Map<RecipeCapability<?>, List<Content>> inputs = tuplesToMap(readCollection(buf, MachineRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickInputs = tuplesToMap(readCollection(buf, MachineRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> outputs = tuplesToMap(readCollection(buf, MachineRecipeSerializer::entryReader));
        Map<RecipeCapability<?>, List<Content>> tickOutputs = tuplesToMap(readCollection(buf, MachineRecipeSerializer::entryReader));
        List<RecipeCondition<?>> conditions = readCollection(buf, RecipeCondition::fromNetwork);
        Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics =
                logicTuplesToMap(readCollection(buf, MachineRecipeSerializer::chanceLogicEntryReader));
        Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics =
                logicTuplesToMap(readCollection(buf, MachineRecipeSerializer::chanceLogicEntryReader));
        Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics =
                logicTuplesToMap(readCollection(buf, MachineRecipeSerializer::chanceLogicEntryReader));
        Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics =
                logicTuplesToMap(readCollection(buf, MachineRecipeSerializer::chanceLogicEntryReader));
        RecipeDataMap data = RecipeDataMap.read(buf);
        int groupColor = buf.readInt();
        ResourceLocation categoryLocation = buf.readResourceLocation();

        MachineRecipeType type = (MachineRecipeType) BuiltInRegistries.RECIPE_TYPE.get(recipeType);
        RecipeCategory category = EFRegistries.RECIPE_CATEGORIES.get(categoryLocation);
        MachineRecipe recipe = new MachineRecipe(type, id,
                inputs, outputs, tickInputs, tickOutputs,
                inputChanceLogics, outputChanceLogics, tickInputChanceLogics, tickOutputChanceLogics,
                conditions, data, duration, category, groupColor);
        recipe.recipeCategory.addRecipe(recipe);
        return recipe;
    }

    public static void toNetwork(RegistryFriendlyByteBuf buf, MachineRecipe recipe) {
        buf.writeResourceLocation(recipe.recipeType.registryName);
        buf.writeResourceLocation(recipe.id);
        buf.writeVarInt(recipe.duration);
        writeCollection(recipe.inputs.entrySet(), buf, MachineRecipeSerializer::entryWriter);
        writeCollection(recipe.tickInputs.entrySet(), buf, MachineRecipeSerializer::entryWriter);
        writeCollection(recipe.outputs.entrySet(), buf, MachineRecipeSerializer::entryWriter);
        writeCollection(recipe.tickOutputs.entrySet(), buf, MachineRecipeSerializer::entryWriter);
        writeCollectionWithMember(recipe.conditions, buf, RecipeCondition::toNetwork);
        writeCollection(recipe.inputChanceLogics.entrySet(), buf, MachineRecipeSerializer::chanceLogicEntryWriter);
        writeCollection(recipe.outputChanceLogics.entrySet(), buf, MachineRecipeSerializer::chanceLogicEntryWriter);
        writeCollection(recipe.tickInputChanceLogics.entrySet(), buf, MachineRecipeSerializer::chanceLogicEntryWriter);
        writeCollection(recipe.tickOutputChanceLogics.entrySet(), buf, MachineRecipeSerializer::chanceLogicEntryWriter);
        recipe.data.write(buf);
        buf.writeInt(recipe.groupColor);
        buf.writeResourceLocation(recipe.recipeCategory.registryKey);
    }

    public static <T> ArrayList<T> readCollection(RegistryFriendlyByteBuf buf,
                                                  net.minecraft.network.codec.StreamDecoder<? super RegistryFriendlyByteBuf, T> decoder) {
        int len = buf.readVarInt();
        ArrayList<T> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            list.add(decoder.decode(buf));
        }
        return list;
    }

    public static <T> void writeCollection(Collection<T> collection, RegistryFriendlyByteBuf buf,
                                           net.minecraft.network.codec.StreamEncoder<? super RegistryFriendlyByteBuf, T> encoder) {
        buf.writeVarInt(collection.size());
        for (T value : collection) {
            encoder.encode(buf, value);
        }
    }

    public static <T> void writeCollectionWithMember(Collection<T> collection, RegistryFriendlyByteBuf buf,
                                                     net.minecraft.network.codec.StreamMemberEncoder<? super RegistryFriendlyByteBuf, T> encoder) {
        buf.writeVarInt(collection.size());
        for (T value : collection) {
            encoder.encode(value, buf);
        }
    }
}
