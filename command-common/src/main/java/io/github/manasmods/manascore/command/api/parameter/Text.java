/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.command.api.parameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Text {
    enum Type {
        /**
         * Single Word Argument
         */
        WORD,
        /**
         * Quoted String Argument
         */
        STRING,
        /**
         * Greedy String Argument
         */
        GREEDY_STRING;
    }

    /**
     * Type of the Argument
     */
    Type value() default Type.WORD;

    /**
     * Argument Name in the Command
     */
    String name() default "";
}