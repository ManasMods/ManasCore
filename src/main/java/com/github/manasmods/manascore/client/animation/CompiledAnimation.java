package com.github.manasmods.manascore.client.animation;

import lombok.Getter;

import java.util.List;

@Getter
public class CompiledAnimation {

    private final String name;

    private final boolean cancelable;

    private final int durationTicks;

    private final List<CompiledAnimationPart> animationParts;

    public CompiledAnimation(String name, boolean cancelable, int durationTicks, List<CompiledAnimationPart> animationParts) {
        this.name = name;
        this.cancelable = cancelable;
        this.durationTicks = durationTicks;

        this.animationParts = animationParts;
    }

}
