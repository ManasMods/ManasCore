package com.github.manasmods.manascore.api.utils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class StreamUtils {
    public StreamUtils() {
    }
    public static <T> Predicate<T> distinctBy(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return (t) -> {
            return seen.add(keyExtractor.apply(t));
        };
    }
}
