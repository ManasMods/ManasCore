/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.command.api;

import lombok.Getter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Permission {
    /**
     * Permission Node String
     */
    String value();

    PermissionLevel permissionLevel() default PermissionLevel.OWNER;

    @Getter
    enum PermissionLevel {
        OWNER(4),
        ADMIN(3),
        GAMEMASTER(2),
        MODERATOR(1),
        PLAYER(0);

        private final int level;

        PermissionLevel(int level) {
            this.level = level;
        }
    }
}
