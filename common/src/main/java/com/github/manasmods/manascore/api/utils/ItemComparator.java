package com.github.manasmods.manascore.api.utils;

import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ItemComparator<T extends Item> {
    private final BiFunction<T, T, Integer>[] tests;

    private ItemComparator(BiFunction<T, T, Integer>[] tests) {
        this.tests = tests;
    }

    public int compare(T object1, T object2) {
        for (BiFunction<T, T, Integer> test : this.tests) {
            int testResult = test.apply(object1, object2);
            if (testResult != 0) return testResult;
        }

        return 0;
    }

    public static class Builder<T extends Item> {
        private final List<BiFunction<T, T, Integer>> tests = new ArrayList<>();

        private Builder(BiFunction<T, T, Integer> initialTest) {
            tests.add(initialTest);
        }

        public static <T extends Item> Builder<T> first(BiFunction<T, T, Integer> initialTest) {
            return new Builder<>(initialTest);
        }

        public static <T extends Item> Builder<T> firstInstancesOf(Class<? extends Item> type) {
            return new Builder<>((t, t2) -> {
                if (type.isInstance(t) && !type.isInstance(t2)) return -1;
                if (type.isInstance(t2) && !type.isInstance(t)) return 1;
                return 0;
            });
        }

        public static <T extends Item> Builder<T> firstInstancesOf(Class<? extends Item> type, boolean exact) {
            if (!exact) return firstInstancesOf(type);

            return new Builder<>((t, t2) -> {
                if (type == t.getClass() && type != t2.getClass()) return -1;
                if (type == t2.getClass() && type != t.getClass()) return 1;
                return 0;
            });
        }

        public Builder<T> then(BiFunction<T, T, Integer> test) {
            tests.add(test);
            return this;
        }

        public Builder<T> thenInstancesOf(Class<? extends Item> type) {
            tests.add((t, t2) -> {
                if (type.isInstance(t) && !type.isInstance(t2)) return -1;
                if (type.isInstance(t2) && !type.isInstance(t)) return 1;
                return 0;
            });

            return this;
        }

        public Builder<T> thenInstancesOf(Class<? extends Item> type, boolean exact) {
            if (!exact) return thenInstancesOf(type);

            tests.add((t, t2) -> {
                if (type == t.getClass() && type != t2.getClass()) return -1;
                if (type == t2.getClass() && type != t.getClass()) return 1;
                return 0;
            });

            return this;
        }

        public ItemComparator<T> build() {
            return new ItemComparator<>(this.tests.toArray(BiFunction[]::new));
        }
    }
}