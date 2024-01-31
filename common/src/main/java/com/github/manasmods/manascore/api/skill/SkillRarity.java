package com.github.manasmods.manascore.api.skill;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SkillRarity {
    Common(0xAAAAAA),
    Uncommon(0x4BCB6B),
    Rare(0x4F6D7A),
    Epic(0x7E53C1),
    Legendary(0xFFD700),
    Mythic(0xFF4500),
    Divine(0xFF00FF),
    Unique(0x00CED1);


    @Getter
    private final int color;
}
