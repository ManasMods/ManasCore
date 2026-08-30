/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.module;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.manasmods.manascore.command.api.*;
import io.github.manasmods.manascore.command.api.parameter.*;
import io.github.manasmods.manascore.command.api.parameter.coordinate.BlockPosArg;
import io.github.manasmods.manascore.command.api.parameter.coordinate.RotationArg;
import io.github.manasmods.manascore.command.api.parameter.coordinate.Vec3Arg;
import io.github.manasmods.manascore.command.api.parameter.primitive.*;
import io.github.manasmods.manascore.command.api.parameter.resource.EnchantmentArg;
import io.github.manasmods.manascore.race.api.SpawnPointHelper;
import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.Skills;
import io.github.manasmods.manascore.testing.ManasCoreTesting;
import io.github.manasmods.manascore.testing.registry.RegistryTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class CommandModuleTest {
    private static final Component RESPONSE = Component.literal("Works!");

    public static void init() {
        CommandArgumentRegistrationEvent.EVENT.register((registry, dispatcher, buildContext) -> {
            registry.registerEnum(TestEnum.class);
        });

        CommandRegistry.registerCommand(TestCommand.class);
    }

    @Command(value = "foo", subCommands = {TestSubCommand.class})
    public static class TestCommand {
        @Execute
        public boolean withPerms(@SenderArg CommandSourceStack sender) {
            sender.sendSystemMessage(RESPONSE);
            return true;
        }
    }

    @Command(value = "bar")
    public static class TestSubCommand {
        @Permission(value = "manascore.command.test", permissionLevel = Permission.PermissionLevel.GAMEMASTER)
        @Execute
        public boolean withPerms(@SenderArg CommandSourceStack sender, @LiteralArg("perms") String l) {
            sender.sendSystemMessage(RESPONSE);
            return true;
        }

        @Execute
        public boolean entityArg(@SenderArg CommandSourceStack sender, @LiteralArg("entity") String l,
                                 @EntityArg(name = "learner", value = EntityArg.Type.PLAYER) EntitySelector selector) throws CommandSyntaxException {
            sender.sendSystemMessage(RESPONSE);
            Entity entity = selector.findSingleEntity(sender);
            if (entity instanceof LivingEntity living) {
                Skills storage = SkillAPI.getSkillsFrom(living);
                if (storage.learnSkill(RegistryTest.TEST_SKILL.get()))
                    ManasCoreTesting.LOG.info("Added Test Skill to " + entity.getName());
            }
            return true;
        }

        @Execute
        public boolean blockPosArg(@SenderArg CommandSourceStack sender, @LiteralArg("pos") String l,
                                   @BlockPosArg BlockPos pos, @RotationArg("x") Coordinates xRot, @RotationArg("y") Coordinates yRot) {
            sender.sendSystemMessage(RESPONSE);
            if (sender.getPlayer() != null)
                SpawnPointHelper.teleportToAcrossDimensions(sender.getPlayer(), Level.OVERWORLD,
                        pos.getX(), pos.getY(), pos.getZ(), xRot.getRotation(sender).x, yRot.getRotation(sender).y);
            return true;
        }

        @Execute
        public boolean vec3Arg(@SenderArg CommandSourceStack sender, @LiteralArg("vec3") String l,
                               @Vec3Arg(value = Vec3Arg.Type.CENTER) Vec3 pos, @DimensionArg ServerLevel dimension) {
            sender.sendSystemMessage(RESPONSE);
            if (sender.getPlayer() != null)
                SpawnPointHelper.teleportToAcrossDimensions(sender.getPlayer(), dimension, pos.x(), pos.y(), pos.z(), 0, 0);
            return true;
        }

        @Execute
        public boolean resourceLocationArg(@SenderArg CommandSourceStack sender, @LiteralArg("resourceLocation") String l,
                                           @ResourceLocationArg ResourceLocation location) {
            sender.sendSystemMessage(RESPONSE);
            ManasSkill skill = SkillAPI.getSkillRegistry().get(location);
            if (skill != null && sender.getPlayer() != null) {
                SkillAPI.getSkillsFrom(sender.getPlayer()).forgetSkill(skill);
                ManasCoreTesting.LOG.info("Removed Test Skill from " + sender.getPlayer().getName());
            }
            return true;
        }

        @Execute
        public boolean itemArg(@SenderArg CommandSourceStack sender, @LiteralArg("item") String l,
                               @ItemArg ItemInput itemInput, @ItemArg("item2") ItemInput itemInput2, @ItemArg("item3") ItemInput itemInput3,
                               @EnchantmentArg Holder.Reference<Enchantment> location) throws CommandSyntaxException {
            sender.sendSystemMessage(RESPONSE);
            if (sender.getPlayer() != null) {
                ItemStack stack = itemInput.createItemStack(5, false);
                stack.enchant(location, 100);
                sender.getPlayer().addItem(stack);
                sender.getPlayer().addItem(itemInput2.createItemStack(4, false));
                sender.getPlayer().addItem(itemInput3.createItemStack(3, false));
            }
            return true;
        }

        @Execute
        public boolean uuidArg(@SenderArg CommandSourceStack sender, @LiteralArg("uuid") String l, @UuidArg UUID uuid) {
            sender.sendSystemMessage(RESPONSE);
            return true;
        }

        @Execute
        public boolean enumArg(@SenderArg CommandSourceStack sender, @LiteralArg("enum") String l,
                               @EnumArg(TestEnum.class) TestEnum _enum, @EnumArg(TestEnum.class) TestEnum _enum2) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nEnum: '")
                    .append(String.valueOf(_enum))
                    .append("'\nEnum 2: '")
                    .append(String.valueOf(_enum2))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean boolArg(@SenderArg CommandSourceStack sender, @LiteralArg("bool") String l, @BooleanArg Boolean bool) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nBoolean: '")
                    .append(bool ? "true" : "false")
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean primitiveBoolArg(@SenderArg CommandSourceStack sender, @LiteralArg("primitive_bool") String l, @BooleanArg boolean bool) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nBoolean (primitive): '")
                    .append(bool ? "true" : "false")
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean wordArg(@SenderArg CommandSourceStack sender, @LiteralArg("word") String l, @TextArg String word) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nWord: '")
                    .append(word)
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean stringArg(@SenderArg CommandSourceStack sender, @LiteralArg("string") String l, @TextArg(TextArg.Type.STRING) String string) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nString: '")
                    .append(string)
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean greedyStringArg(@SenderArg CommandSourceStack sender, @LiteralArg("greedy_string") String l, @TextArg(TextArg.Type.GREEDY_STRING) String greedyString) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nGreedy String: '")
                    .append(greedyString)
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean doubleArg(@SenderArg CommandSourceStack sender, @LiteralArg("double") String l, @DoubleArg Double d, @DoubleArg Double d2) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nDouble: '")
                    .append(String.valueOf(d))
                    .append("'\nDouble 2: '")
                    .append(String.valueOf(d2))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean primitiveDoubleArg(@SenderArg CommandSourceStack sender, @LiteralArg("primitive_double") String l, @DoubleArg double d) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nDouble (primitive): '")
                    .append(String.valueOf(d))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean floatArg(@SenderArg CommandSourceStack sender, @LiteralArg("float") String l, @FloatArg Float f) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nFloat: '")
                    .append(String.valueOf(f))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean primitiveFloatArg(@SenderArg CommandSourceStack sender, @LiteralArg("primitive_float") String l, @FloatArg float f) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nFloat (primitive): '")
                    .append(String.valueOf(f))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean intArg(@SenderArg CommandSourceStack sender, @LiteralArg("int") String l, @IntegerArg Integer i) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nInteger: '")
                    .append(String.valueOf(i))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean primitiveIntArg(@SenderArg CommandSourceStack sender, @LiteralArg("primitive_int") String l, @IntegerArg int i) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nInteger (primitive): '")
                    .append(String.valueOf(i))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean longArg(@SenderArg CommandSourceStack sender, @LiteralArg("long") String l, @LongArg Long l1) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nLong: '")
                    .append(String.valueOf(l1))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean primitiveLongArg(@SenderArg CommandSourceStack sender, @LiteralArg("primitive_long") String l, @LongArg long l1) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nLong (primitive): '")
                    .append(String.valueOf(l1))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean multiple(@SenderArg CommandSourceStack sender, @LiteralArg("literal") String l, @EnumArg(TestEnum.class) TestEnum _enum) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nEnum: '")
                    .append(_enum.name())
                    .append("'")
            );
            return true;
        }
    }

    public enum TestEnum {
        TEST,
        TEST2,
        TEST3
    }
}
