package com.github.manasmods.manascore.api.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * <h1>Register</h1>
 * <p>A builder class for registering content.</p>
 * <p>
 * <h2>Usage</h2>
 * <ol>
 *     <li>Create a new instance of this class in your mod's main class and store it in a static variable.</li>
 *     <li>Call the {@link AbstractRegister#init()} method in your mod's initialization point.</li>
 *     <li>Use the builder methods of your {@link Register} instance to create {@link Item}s, {@link Block}s, {@link EntityType}s, ...</li>
 * </ol>
 *
 * @see AbstractRegister
 * @see AbstractRegister#init()
 */
public class Register extends AbstractRegister<Register> {
    public Register(String modId) {
        super(modId);
    }
}
