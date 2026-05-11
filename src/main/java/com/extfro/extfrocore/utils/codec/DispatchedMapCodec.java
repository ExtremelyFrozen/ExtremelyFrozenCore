package com.extfro.extfrocore.utils.codec;

import com.extfro.extfrocore.ExtForCore;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public record DispatchedMapCodec<K, V>(Codec<K> keyCodec, Function<K, Codec<? extends V>> valueCodecFunction)
        implements Codec<Map<K, V>> {

    @Override
    public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix) {
        RecordBuilder<T> mapBuilder = ops.mapBuilder();
        for (Map.Entry<K, V> entry : input.entrySet()) {
            mapBuilder.add(keyCodec.encodeStart(ops, entry.getKey()),
                    encodeValue(valueCodecFunction.apply(entry.getKey()), entry.getValue(), ops));
        }
        return mapBuilder.build(prefix);
    }

    @SuppressWarnings("unchecked")
    private <T, V2 extends V> DataResult<T> encodeValue(Codec<V2> codec, V input, DynamicOps<T> ops) {
        return codec.encodeStart(ops, (V2) input);
    }

    @Override
    public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getMap(input).flatMap(map -> {
            Map<K, V> entries = new Object2ObjectArrayMap<>();
            Stream.Builder<Pair<T, T>> failed = Stream.builder();

            DataResult<Unit> finalResult = map.entries().reduce(
                    DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
                    (result, entry) -> parseEntry(result, ops, entry, entries, failed),
                    (left, right) -> left.apply2stable((ignoredLeft, ignoredRight) -> ignoredLeft, right));

            Pair<Map<K, V>, T> pair = Pair.of(new Object2ObjectArrayMap<>(entries), input);
            T errors = ops.createMap(failed.build());

            return finalResult.map(ignored -> pair).setPartial(pair)
                    .mapError(error -> error + " missed input: " + errors);
        });
    }

    private <T> DataResult<Unit> parseEntry(DataResult<Unit> result, DynamicOps<T> ops, Pair<T, T> input,
                                            Map<K, V> entries, Stream.Builder<Pair<T, T>> failed) {
        DataResult<K> keyResult = keyCodec.parse(ops, input.getFirst());
        DataResult<V> valueResult = keyResult.map(valueCodecFunction)
                .flatMap(valueCodec -> valueCodec.parse(ops, input.getSecond()).map(Function.identity()));
        DataResult<Pair<K, V>> entryResult = keyResult.apply2stable(Pair::of, valueResult);

        Optional<Pair<K, V>> entry = entryResult.resultOrPartial(ExtForCore.LOGGER::error);
        if (entry.isPresent()) {
            K key = entry.get().getFirst();
            V value = entry.get().getSecond();
            if (entries.putIfAbsent(key, value) != null) {
                failed.add(input);
                return result.apply2stable((ignored, pair) -> ignored,
                        DataResult.error(() -> "Duplicate entry for key: '" + key + "'"));
            }
        }
        if (entryResult.error().isPresent()) {
            failed.add(input);
        }

        return result.apply2stable((ignored, pair) -> ignored, entryResult);
    }
}
