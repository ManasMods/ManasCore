/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.command.internal;

import io.github.manasmods.manascore.command.api.Permission;
import io.github.manasmods.manascore.command.api.Permission.PermissionLevel;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PermissionCheckTest {
    @Test
    void nonPlayerPasses() throws Exception {
        FakeSource source = new FakeSource(false, false, 0, Map.of("test.main", false));
        assertTrue(PermissionCheck.isAllowed(source, this.getPermission("gamemaster")));
    }

    @Test
    void noPermissionModMatchesOpLevel() throws Exception {
        FakeSource operator = new FakeSource(true, true, 2, Map.of("test.main", true));
        FakeSource player = new FakeSource(true, true, 0, Map.of("test.main", false));
        assertTrue(PermissionCheck.isAllowed(operator, this.getPermission("gamemaster")));
        assertFalse(PermissionCheck.isAllowed(player, this.getPermission("gamemaster")));
    }

    @Test
    void explicitFalseDeniesOperatorOnPlayerNode() throws Exception {
        FakeSource source = new FakeSource(true, true, 4, Map.of("test.main", false));
        assertFalse(PermissionCheck.isAllowed(source, this.getPermission("player")));
    }

    @Test
    void borrowedSourceKeepsStackLevel() throws Exception {
        FakeSource source = new FakeSource(true, false, 4, Map.of("test.main", false));
        assertTrue(PermissionCheck.isAllowed(source, this.getPermission("gamemaster")));
    }

    @Test
    void elevatedOwnStackKeepsStackLevel() throws Exception {
        FakeSource source = new FakeSource(true, true, 2, 0, Map.of("test.main", false));
        assertTrue(PermissionCheck.isAllowed(source, this.getPermission("gamemaster")));
    }

    @Test
    void elevatedOwnStackStillChecksAboveItsLevel() throws Exception {
        FakeSource source = new FakeSource(true, true, 2, 0, Map.of("test.main", false));
        assertFalse(PermissionCheck.isAllowed(source, this.getPermission("admin")));
    }

    @Test
    void explicitTrueAllowsNonOp() throws Exception {
        FakeSource source = new FakeSource(true, true, 0, Map.of("test.main", true));
        assertTrue(PermissionCheck.isAllowed(source, this.getPermission("gamemaster")));
    }

    @Test
    void anyOfGrantsWhenMainDenied() throws Exception {
        FakeSource source = new FakeSource(true, true, 4, Map.of("test.main", false, "test.alt", true));
        assertTrue(PermissionCheck.isAllowed(source, this.getPermission("playerWithAlt")));
    }

    @Test
    void allNodesFalseDenies() throws Exception {
        FakeSource source = new FakeSource(true, true, 4, Map.of("test.main", false, "test.alt", false));
        assertFalse(PermissionCheck.isAllowed(source, this.getPermission("playerWithAlt")));
    }

    @Test
    void unregisteredMainFallsBackToLevel() throws Exception {
        FakeSource operator = new FakeSource(true, true, 2, Map.of());
        FakeSource moderator = new FakeSource(true, true, 1, Map.of());
        assertTrue(PermissionCheck.isAllowed(operator, this.getPermission("gamemaster")));
        assertFalse(PermissionCheck.isAllowed(moderator, this.getPermission("gamemaster")));
    }

    @Test
    void explicitLevelWithoutValueDeniesOperator() throws Exception {
        FakeSource source = new FakeSource(true, true, 4, Map.of());
        assertFalse(PermissionCheck.isAllowed(source, this.getPermission("explicit")));
    }

    private Permission getPermission(String name) throws Exception {
        return PermissionCheckTest.class.getDeclaredMethod(name).getAnnotation(Permission.class);
    }

    @Permission(value = "test.main", permissionLevel = PermissionLevel.ADMIN)
    public static void admin() {}

    @Permission(value = "test.main", permissionLevel = PermissionLevel.GAMEMASTER)
    public static void gamemaster() {}

    @Permission(value = "test.main", permissionLevel = PermissionLevel.PLAYER)
    public static void player() {}

    @Permission(value = "test.main", permissionLevel = PermissionLevel.PLAYER, anyOf = "test.alt")
    public static void playerWithAlt() {}

    @Permission(value = "test.main", permissionLevel = PermissionLevel.EXPLICIT)
    public static void explicit() {}

    public static class FakeSource implements IPermissionSource {
        private final boolean player;
        private final boolean own;
        private final int opLevel;
        private final int ownLevel;
        private final Map<String, Boolean> values;

        public FakeSource(boolean player, boolean own, int opLevel, Map<String, Boolean> values) {
            this(player, own, opLevel, opLevel, values);
        }

        public FakeSource(boolean player, boolean own, int opLevel, int ownLevel, Map<String, Boolean> values) {
            this.player = player;
            this.own = own;
            this.opLevel = opLevel;
            this.ownLevel = ownLevel;
            this.values = values;
        }

        public boolean isPlayer() {
            return this.player;
        }

        public boolean isOwnCommand() {
            return this.own;
        }

        public boolean hasLevel(int level) {
            return this.opLevel >= level;
        }

        public boolean hasOwnLevel(int level) {
            return this.ownLevel >= level;
        }

        public @Nullable Boolean getValue(String node) {
            return this.values.get(node);
        }
    }
}
