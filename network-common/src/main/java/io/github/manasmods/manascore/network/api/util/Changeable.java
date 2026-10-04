/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.network.api.util;

import org.jetbrains.annotations.Nullable;

public class Changeable<T> {
    @Nullable
    private final T original;
    private T value;

    protected Changeable(@Nullable T value) {
        this.original = value;
        this.value = value;
    }

    public static <T> Changeable<T> of(@Nullable T value) {
        return new Changeable<>(value);
    }

    @Nullable
    public T get() {
        return value;
    }

    public void set(@Nullable T value) {
        this.value = value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public boolean hasChanged() {
        if (original == null) return value != null;
        return !original.equals(value);
    }
}
