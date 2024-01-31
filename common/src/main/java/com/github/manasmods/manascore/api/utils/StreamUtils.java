package com.github.manasmods.manascore.api.utils;

import lombok.experimental.UtilityClass;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@UtilityClass
public class StreamUtils {
    public static <T> Predicate<T> distinctBy(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return (value) -> seen.add(keyExtractor.apply(value));
    }
}
