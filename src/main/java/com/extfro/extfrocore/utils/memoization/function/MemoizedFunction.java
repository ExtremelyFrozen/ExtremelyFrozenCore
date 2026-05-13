package com.extfro.extfrocore.utils.memoization.function;

import java.util.Map;
import java.util.function.Function;

public interface MemoizedFunction<T, R> extends Function<T, R> {

    Map<T, R> getCache();
}
