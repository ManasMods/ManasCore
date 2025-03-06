/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.command.api.parameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface EntityArg {
    /**
     * Argument Name in the Command
     */
    String name() default "entity";

    /**
     * Type of the Argument
     */
    Type value() default Type.ENTITY;

    enum Type {
        /**
         * Single Entity
         */
        ENTITY,
        /**
         * Collection of Entities
         */
        ENTITIES,
        /**
         * Single Player
         */
        PLAYER,
        /**
         * Collection of Players
         */
        PLAYERS
    }
}