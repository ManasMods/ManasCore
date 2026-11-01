/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.animation.api;

import com.google.common.collect.MapMaker;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This is the client side runtime for Bedrock format player animations.
 * It is loader agnostic.
 * <p>
 * The animation JSONs are loaded through the client {@link ResourceManager} from the {@code assets/<modid>/manas_animations/}
 * directory of every mod and keyed as {@code <modid>:<animation_name>}.
 * Using the resource manager instead of walking the disk makes it work across all Architectury modules in dev and production.
 * <p>
 * Use {@link PlayerAnimationHelper} on the server to play an animation.
 */
public class PlayerAnimationAPI {
    public static final Logger LOG = LoggerFactory.getLogger("ManasCore Animation");
    /**
     * Every loaded animation keyed as {@code <modid>:<animation_name>}.
     */
    public static final Map<String, PlayerAnimation> animations = new Object2ObjectOpenHashMap<>();
    /**
     * Animation currently playing on each entity.
     * Keyed by {@link LivingEntity} instead of {@code Player}, so everything drawn with a {@link net.minecraft.client.model.PlayerModel}
     * (clones, player-like mobs) animates through the same pipeline as a player.
     * <p>
     * Weakly keyed, because nothing removes entries when an entity is unloaded and mob shaped users would grow it without bound.
     * Keys compare by identity, not {@code Entity#equals}, which compares entity ids: a respawned or dimension changed player
     * is a new entity with the old id and would otherwise inherit the dead one's animation. Null values are not allowed.
     */
    public static final Map<LivingEntity, PlayerAnimation> active_animations = new MapMaker().weakKeys().makeMap();

    /** Client-side per-entity playback state. Keyed the same way as {@link #active_animations}. */
    public static final Map<LivingEntity, PlayerAnimationState> states = new MapMaker().weakKeys().makeMap();

    /**
     * Set for the duration of {@code LevelRenderer#renderLevel}.
     * Client code uses it to tell a player drawn into the world from the same player drawn into a GUI widget or a HUD overlay.
     * Those run outside {@code renderLevel}, in {@code Gui#render} and screen rendering, and must show the whole model instead of the arms only first-person pose.
     * <p>
     * This tells the two draws apart, {@code Minecraft#screen} can not, because the world still renders behind an open screen.
     * Nothing outside {@link io.github.manasmods.manascore.animation.mixin.client.MixinLevelRendererAnimation} should write this.
     */
    public static boolean renderingLevel = false;

    public static boolean isFirstPersonActive(LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        // An open screen shows the vanilla hands instead
        return entity == mc.player && mc.screen == null && state(entity).firstPerson;
    }

    /**
     * Reserved entry for {@code manascore:requires_animation} that means nothing is currently playing.
     * It cannot collide with a real animation, because every real animation key is {@code <modid>:<name>}.
     */
    public static final String NO_ANIMATION = "none";

    /**
     * Used to get the {@link PlayerAnimationState} of the {@link LivingEntity}.
     * A new state is created if the entity has none.
     */
    public static PlayerAnimationState state(LivingEntity entity) {
        return states.computeIfAbsent(entity, key -> new PlayerAnimationState());
    }

    /**
     * Determine if {@code animationKey} can start while {@code currentAnimation} is playing, according to the
     * {@code manascore:requires_animation} allowlist of the requested animation.
     * <p>
     * It is evaluated on the client, because only the client knows what is playing. The server tracks no animation state.
     * A blocked request is dropped silently, the caller has already sent its packet and does not find out.
     * <p>
     * Only the packet driven {@code currentAnimation} counts as playing.
     * A conditional animation is the ambient fallback for when nothing is playing, so it reads as {@link #NO_ANIMATION} here.
     *
     * @param animationKey     Animation being requested.
     * @param currentAnimation Animation currently playing, or empty for none.
     * @return false will drop the request to play {@code animationKey}.
     */
    public static boolean canPlay(String animationKey, String currentAnimation) {
        PlayerAnimation animation = animations.get(animationKey);
        if (animation == null || animation.requiresAnimation.isEmpty()) return true;
        if (currentAnimation.isEmpty()) return animation.requiresAnimation.contains(NO_ANIMATION);
        return animation.requiresAnimation.contains(currentAnimation);
    }

    /**
     * Used to apply the {@code body} bone of the active animation to {@code poseStack} as a whole entity transform.
     * Call it at the point where the entity has just been oriented (after the body yaw rotation, before the model scale flip).
     * <p>
     * {@code body} is the root of the rig and not a {@link net.minecraft.client.model.geom.ModelPart}, so it has to land on the pose stack.
     * This is public so renderers that are not a {@code LivingEntityRenderer} (GeckoLib) can apply root motion too.
     *
     * @param entity    Affected {@link LivingEntity} playing the animation.
     * @param poseStack Affected {@link PoseStack} to transform.
     * @return true if a root transform was applied.
     */
    public static boolean applyRootBone(LivingEntity entity, PoseStack poseStack) {
        PlayerAnimation animation = active_animations.get(entity);
        if (animation == null) return false;
        PlayerBone bone = animation.bones.get("body");
        if (bone == null) return false;

        // Scale body
        float progress = state(entity).progress;
        Vec3 boneScale = PlayerBone.interpolate(bone.scales, progress, entity);
        if (boneScale != null) poseStack.scale((float) boneScale.x, (float) boneScale.y, (float) boneScale.z);

        // Move body, raised by 0.75 as the rotation pivot
        Vec3 position = PlayerBone.interpolate(bone.positions, progress, entity);
        if (position != null) poseStack.translate((float) -position.x * 0.0625f, (float) (position.y * 0.0625f) + 0.75f, (float) position.z * 0.0625f);

        // Rotate body
        Vec3 rotation = PlayerBone.interpolate(bone.rotations, progress, entity);
        if (rotation != null) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) rotation.z));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) -rotation.y));
            poseStack.mulPose(Axis.XP.rotationDegrees((float) -rotation.x));
        }

        // Undo the pivot raise
        if (position != null) poseStack.translate(0, -0.75f, 0);
        return true;
    }

    private static int suppressDepth = 0;

    /**
     * Used to run {@code action} with ManasCore player animations disabled for every {@code PlayerModel#setupAnim} call it makes.
     * The model gets the pure vanilla pose and no animation state is read or advanced: no progress, no sounds, no conditional evaluation.
     * <p>
     * This can be nested and must only be called on the client render thread.
     *
     * @param action Code that poses or renders a player model without animations.
     */
    public static void withoutAnimations(Runnable action) {
        suppressDepth++;
        try {
            action.run();
        } finally {
            suppressDepth--;
        }
    }

    /**
     * Determine if the current code is running inside {@link #withoutAnimations}.
     */
    public static boolean isSuppressed() {
        return suppressDepth > 0;
    }

    /**
     * Client side playback state of one {@link LivingEntity}.
     * It is written by {@code AnimationClientHandler} when a packet arrives and advanced by {@code MixinPlayerModel} while the level renders.
     */
    public static class PlayerAnimationState {
        /** Key of the packet driven animation. Empty for none. */
        public String currentAnimation = "";
        /** Key that plays automatically when {@link #currentAnimation} finishes. Empty for none. */
        public String nextAnimation = "";
        /** Key of the conditional animation that is playing. Empty for none. */
        public String currentConditional = "";
        /** true restarts the animation on the next pose. Cleared once applied. */
        public boolean override = false;
        /** true shows the playing animation from the first person view of the owner. */
        public boolean firstPerson = false;
        /** true clears the active animation on the next pose. Cleared once applied. */
        public boolean reset = false;
        /** true once the playback has started and {@link #progress} is valid. */
        public boolean hasProgress = false;
        /** Playback time of the animation in seconds. */
        public float progress = 0f;
        /** Value of {@code ageInTicks} at the last advance. */
        public float lastTickTime = 0f;
        /** Value of {@link #progress} at the last check of the sound effects. */
        public float lastAnimationProgress = 0f;
        /** Times of the sound effects that already played in the current loop. */
        public final Set<Float> playedSounds = new HashSet<>();
    }

    /**
     * Used to load the animations of the {@code manas_animations} directory of every mod, replacing the previously loaded ones.
     * It is called by the client resource reload listener. A resource that fails to load is logged and skipped.
     *
     * @param manager Affected {@link ResourceManager} to load the animation JSONs from.
     */
    public static void loadClientSideAnimations(ResourceManager manager) {
        animations.clear();
        Map<ResourceLocation, Resource> found = manager.listResources("manas_animations",
                location -> location.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : found.entrySet()) {
            ResourceLocation location = entry.getKey();
            try (InputStream stream = entry.getValue().open()) {
                String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                loadContent(location.getNamespace(), content);
            } catch (Exception e) {
                LOG.error("Failed to load animation resource: {} - {}", location, e.getMessage());
            }
        }
    }

    /**
     * Used to parse one animation file and register its animations as {@code <modid>:<animation_name>}.
     */
    private static void loadContent(String modId, String content) {
        try {
            JsonObject jsonObject = new Gson().fromJson(content, JsonObject.class);
            JsonObject sourceAnimations = jsonObject.getAsJsonObject("animations");
            if (sourceAnimations == null) return;
            for (Map.Entry<String, JsonElement> entry : sourceAnimations.entrySet()) {
                String animationName = modId + ":" + entry.getKey();
                animations.put(animationName, new PlayerAnimation(entry.getValue().getAsJsonObject()));
            }
        } catch (Exception e) {
            LOG.error("Failed to parse animation content for {} - {}", modId, e.getMessage());
        }
    }

    /**
     * One animation, parsed from the JSON object of a Bedrock animation.
     * The {@code manascore:} fields are ManasCore extensions of the format.
     */
    public static class PlayerAnimation {
        /** Length of the animation in seconds. */
        public final float length;
        /** true repeats the animation when it finishes. */
        public boolean loop = false;
        /** true keeps the last frame after the animation finished. */
        public boolean hold_on_last_frame = false;

        /**
         * Populated from {@code manascore:additive_bones}.
         * Bones listed here add their rotation on top of the vanilla pose instead of replacing it.
         * Never null, empty means every bone replaces the vanilla pose.
         */
        public final Set<String> additiveBones;

        /**
         * Populated from {@code manascore:aim_bones}.
         * Bones listed here rotate to follow the look direction of the player, on top of the authored pose.
         * They pivot at their own origin (the shoulder, for an arm).
         * Never null, empty means no bone aims.
         */
        public final Set<String> aimBones;

        /**
         * Populated from {@code manascore:aim_body}.
         * Turns the whole entity to face where the player is looking, horizontally only, the body never pitches.
         * Pairs with {@link #aimBones} which handle the vertical: with the body squared up {@code netHeadYaw} collapses to zero
         * and the listed bones contribute pitch alone.
         * Defaults to false.
         */
        public final boolean aimBody;

        /**
         * Populated from {@code manascore:requires_animation}.
         * Allowlist of animation keys this animation can start from.
         * If it is not empty and the playing animation is not in it, the request to play this animation is dropped.
         * Use {@link #NO_ANIMATION} to allow starting from an idle player.
         * Never null, empty means no restriction, which is the default.
         */
        public final Set<String> requiresAnimation;

        /**
         * Populated from {@code manascore:suppress_attack}.
         * Tri-state: null means the field was absent and callers fall back to the bone heuristic.
         * That is not the same as {@code Boolean.FALSE}, which explicitly means never suppress.
         * Boxed on purpose so the distinction cannot be lost, do not simplify it to a primitive boolean.
         */
        public final Boolean suppressAttack;

        /**
         * Populated from {@code manascore:suppress_crouch}.
         * Defaults to true, so an animation clears {@code crouching} unless it opts out.
         */
        public final boolean suppressCrouch;

        /** Bones of the animation keyed by bone name. */
        public final Map<String, PlayerBone> bones;
        /** Sound events keyed by the time in seconds they play at. */
        public final Map<Float, String> soundEffects;

        /**
         * Used to parse an animation from its JSON object. Missing fields get the defaults documented on the fields.
         */
        public PlayerAnimation(JsonObject animation) {
            // Parse length and loop mode, a non boolean loop value holds the last frame
            if (animation.has("animation_length"))
                this.length = animation.get("animation_length").getAsFloat();
            else
                this.length = 0;
            if (animation.has("loop")) {
                JsonElement loopType = animation.get("loop");
                if (loopType.isJsonPrimitive() && loopType.getAsJsonPrimitive().isBoolean())
                    this.loop = loopType.getAsBoolean();
                else if (loopType.isJsonPrimitive())
                    this.hold_on_last_frame = true;
            }
            // Parse additive bones
            this.additiveBones = new HashSet<>();
            if (animation.has("manascore:additive_bones")) {
                JsonElement additiveElement = animation.get("manascore:additive_bones");
                if (additiveElement.isJsonArray()) {
                    for (JsonElement boneElement : additiveElement.getAsJsonArray()) {
                        if (boneElement.isJsonPrimitive()) {
                            this.additiveBones.add(boneElement.getAsString());
                        }
                    }
                }
            }
            // Parse aim bones
            this.aimBones = new HashSet<>();
            if (animation.has("manascore:aim_bones")) {
                JsonElement aimElement = animation.get("manascore:aim_bones");
                if (aimElement.isJsonArray()) {
                    for (JsonElement boneElement : aimElement.getAsJsonArray()) {
                        if (boneElement.isJsonPrimitive()) {
                            this.aimBones.add(boneElement.getAsString());
                        }
                    }
                }
            }
            // Parse aim body
            boolean parsedAimBody = false;
            if (animation.has("manascore:aim_body")) {
                JsonElement aimBodyElement = animation.get("manascore:aim_body");
                if (aimBodyElement.isJsonPrimitive() && aimBodyElement.getAsJsonPrimitive().isBoolean()) {
                    parsedAimBody = aimBodyElement.getAsBoolean();
                }
            }
            this.aimBody = parsedAimBody;
            // Parse required animations
            this.requiresAnimation = new HashSet<>();
            if (animation.has("manascore:requires_animation")) {
                JsonElement requiresElement = animation.get("manascore:requires_animation");
                if (requiresElement.isJsonArray()) {
                    for (JsonElement requiredElement : requiresElement.getAsJsonArray()) {
                        if (requiredElement.isJsonPrimitive()) {
                            this.requiresAnimation.add(requiredElement.getAsString());
                        }
                    }
                }
            }
            // Parse suppress attack
            Boolean parsedSuppressAttack = null;
            if (animation.has("manascore:suppress_attack")) {
                JsonElement suppressAttackElement = animation.get("manascore:suppress_attack");
                if (suppressAttackElement.isJsonPrimitive() && suppressAttackElement.getAsJsonPrimitive().isBoolean()) {
                    parsedSuppressAttack = suppressAttackElement.getAsBoolean();
                }
            }
            this.suppressAttack = parsedSuppressAttack;
            // Parse suppress crouch
            boolean parsedSuppressCrouch = true;
            if (animation.has("manascore:suppress_crouch")) {
                JsonElement suppressCrouchElement = animation.get("manascore:suppress_crouch");
                if (suppressCrouchElement.isJsonPrimitive() && suppressCrouchElement.getAsJsonPrimitive().isBoolean()) {
                    parsedSuppressCrouch = suppressCrouchElement.getAsBoolean();
                }
            }
            this.suppressCrouch = parsedSuppressCrouch;
            // Parse bones
            this.bones = new HashMap<>();
            if (animation.has("bones")) {
                JsonObject bonesObj = animation.getAsJsonObject("bones");
                for (String boneName : bonesObj.keySet()) {
                    this.bones.put(boneName, new PlayerBone(bonesObj.getAsJsonObject(boneName)));
                }
            }
            // Parse sound effects
            this.soundEffects = new HashMap<>();
            if (animation.has("sound_effects")) {
                JsonObject soundEffectsObj = animation.getAsJsonObject("sound_effects");
                for (Map.Entry<String, JsonElement> entry : soundEffectsObj.entrySet()) {
                    try {
                        float time = Float.parseFloat(entry.getKey());
                        JsonObject soundData = entry.getValue().getAsJsonObject();
                        if (soundData.has("effect")) {
                            soundEffects.put(time, soundData.get("effect").getAsString());
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * One bone of an animation, holding the keyframes of its rotation, position and scale.
     * Values can be constants or Molang expressions that are evaluated for the animated {@link LivingEntity}.
     */
    public static class PlayerBone {
        /** Molang queries that {@link #getQueryValue} can resolve. */
        private static final List<String> MOLANG_QUERIES = List.of("query.anim_time", "query.head_x_rotation", "query.head_y_rotation", "query.body_x_rotation", "query.body_y_rotation",
                "query.life_time", "query.health", "query.max_health", "query.is_on_ground", "query.is_in_water", "query.is_sneaking", "query.is_sprinting", "query.is_swimming",
                "query.is_riding", "query.is_sleeping", "query.is_alive", "query.is_gliding", "query.ground_speed", "query.vertical_speed", "query.speed", "query.limb_swing",
                "query.limb_swing_amount", "query.modified_move_speed", "query.walk_anim_speed", "query.modified_distance_moved", "query.hurt_time", "query.death_time",
                "query.swing_progress", "query.is_using_item", "query.use_item_interval", "query.is_first_person", "query.main_hand_item_use_duration", "query.yaw_speed",
                "query.position_delta_x", "query.position_delta_y", "query.position_delta_z");
        /** Keyframes of the rotation in degrees. */
        public final List<Keyframe> rotations;
        /** Keyframes of the position offset in pixels, which are 1/16 of a block. */
        public final List<Keyframe> positions;
        /** Keyframes of the scale factor. */
        public final List<Keyframe> scales;

        /**
         * Used to parse the keyframes of a bone from its JSON object.
         */
        public PlayerBone(JsonObject bone) {
            this.rotations = parseTransform(bone, "rotation");
            this.positions = parseTransform(bone, "position");
            this.scales = parseTransform(bone, "scale");
        }

        /**
         * One keyframe at {@code time} seconds.
         * {@code pre} and {@code post} are the values before and after the keyframe and default to {@code value}.
         * {@code catmullrom} interpolates from this keyframe to the next one with a Catmull-Rom spline instead of linearly.
         */
        public static class Keyframe {
            public final float time;
            public final KeyframeValue value;
            public final KeyframeValue pre;
            public final KeyframeValue post;
            public final boolean catmullrom;

            public Keyframe(float time, KeyframeValue value, KeyframeValue pre, KeyframeValue post, boolean catmullrom) {
                this.time = time;
                this.value = value;
                this.pre = pre != null ? pre : value;
                this.post = post != null ? post : value;
                this.catmullrom = catmullrom;
            }
        }

        /**
         * Either a constant vector or a Molang expression.
         * {@code queries} lists the Molang queries that the expression uses.
         */
        public static class KeyframeValue {
            public final Vec3 vector;
            public final String molang;
            public final List<String> queries;

            public KeyframeValue(Vec3 vector) {
                this.vector = vector;
                this.molang = null;
                this.queries = List.of();
            }

            public KeyframeValue(String molang) {
                this.molang = molang;
                this.vector = null;
                this.queries = new ArrayList<>();
                for (String query : MOLANG_QUERIES) if (molang.contains(query)) this.queries.add(query);
            }

            /**
             * Determine if this value is a Molang expression and not a constant vector.
             */
            public boolean isMolang() {
                return molang != null;
            }
        }

        /**
         * Used to parse the keyframes of one transform ({@code rotation}, {@code position} or {@code scale}) of a bone.
         * A single value becomes one keyframe at time 0.
         */
        private List<Keyframe> parseTransform(JsonObject bone, String key) {
            List<Keyframe> result = new ArrayList<>();
            if (!bone.has(key)) {
                return result;
            }
            JsonElement element = bone.get(key);
            if (element.isJsonArray()) {
                result.add(new Keyframe(0f, parseValue(element), null, null, false));
            } else if (element.isJsonPrimitive()) {
                result.add(new Keyframe(0f, parseValue(element), null, null, false));
            } else if (element.isJsonObject()) {
                JsonObject keyframes = element.getAsJsonObject();
                for (String timeStr : keyframes.keySet()) {
                    float time = Float.parseFloat(timeStr);
                    JsonElement frameValue = keyframes.get(timeStr);
                    if (frameValue.isJsonArray() || frameValue.isJsonPrimitive()) {
                        result.add(new Keyframe(time, parseValue(frameValue), null, null, false));
                    } else if (frameValue.isJsonObject()) {
                        JsonObject frameObj = frameValue.getAsJsonObject();
                        KeyframeValue value = frameObj.has("post") ? parseValue(frameObj.get("post")) : parseValue(frameValue);
                        KeyframeValue pre = frameObj.has("pre") ? parseValue(frameObj.get("pre")) : null;
                        KeyframeValue post = frameObj.has("post") ? parseValue(frameObj.get("post")) : null;
                        boolean catmullrom = frameObj.has("lerp_mode") && frameObj.get("lerp_mode").getAsString().equalsIgnoreCase("catmullrom");
                        result.add(new Keyframe(time, value, pre, post, catmullrom));
                    }
                }
            }
            return result;
        }

        /**
         * Used to parse one value.
         * An array with a string entry or a string becomes a Molang expression, anything else a constant vector.
         * A single number is applied to all three axes.
         */
        private KeyframeValue parseValue(JsonElement element) {
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                boolean hasMolang = false;
                StringBuilder molangArray = new StringBuilder("[");
                for (int i = 0; i < array.size(); i++) {
                    if (i > 0)
                        molangArray.append(",");
                    JsonElement elem = array.get(i);
                    if (elem.isJsonPrimitive()) {
                        JsonPrimitive prim = elem.getAsJsonPrimitive();
                        if (prim.isString()) {
                            hasMolang = true;
                            molangArray.append(prim.getAsString());
                        } else {
                            molangArray.append(prim.getAsFloat());
                        }
                    }
                }
                molangArray.append("]");
                if (hasMolang)
                    return new KeyframeValue(molangArray.toString());
                float x = array.size() > 0 && array.get(0).isJsonPrimitive() ? array.get(0).getAsFloat() : 0;
                float y = array.size() > 1 && array.get(1).isJsonPrimitive() ? array.get(1).getAsFloat() : 0;
                float z = array.size() > 2 && array.get(2).isJsonPrimitive() ? array.get(2).getAsFloat() : 0;
                return new KeyframeValue(new Vec3(x, y, z));
            }
            if (element.isJsonPrimitive()) {
                JsonPrimitive prim = element.getAsJsonPrimitive();
                if (prim.isString())
                    return new KeyframeValue(prim.getAsString());
                float value = prim.getAsFloat();
                return new KeyframeValue(new Vec3(value, value, value));
            }
            return new KeyframeValue(Vec3.ZERO);
        }

        /**
         * Used to sample the keyframes at {@code time}.
         * It interpolates linearly between the surrounding keyframes, or with a Catmull-Rom spline when the earlier keyframe asks for it.
         * Molang values are evaluated for the {@link LivingEntity}.
         *
         * @param keyframes Keyframes of one transform, in ascending time order.
         * @param time      Playback time of the animation in seconds.
         * @param entity    Affected {@link LivingEntity} that the Molang queries read from.
         * @return the sampled vector, or null if there are no keyframes or {@code time} is before the first one.
         */
        public static Vec3 interpolate(List<Keyframe> keyframes, float time, LivingEntity entity) {
            if (keyframes.isEmpty())
                return null;
            if (keyframes.size() == 1) {
                Keyframe kf = keyframes.get(0);
                return kf.value.isMolang() ? evalMolang(kf.value, time, entity) : kf.value.vector;
            }
            // Find the surrounding keyframes
            Keyframe lastKf = null;
            Keyframe nextKf = null;
            int lastIdx = -1;
            for (int i = 0; i < keyframes.size(); i++) {
                Keyframe kf = keyframes.get(i);
                if (time >= kf.time) {
                    lastKf = kf;
                    lastIdx = i;
                }
                if (time < kf.time) {
                    nextKf = kf;
                    break;
                }
            }
            if (lastKf == null)
                return null;
            Vec3 postVec = lastKf.post.isMolang() ? evalMolang(lastKf.post, time, entity) : lastKf.post.vector;
            if (nextKf == null)
                return postVec;
            float t1 = lastKf.time;
            float t2_ = nextKf.time;
            if (t1 == t2_)
                return postVec;
            float alpha = (time - t1) / (t2_ - t1);
            Vec3 v1 = postVec;
            Vec3 v2 = nextKf.pre.isMolang() ? evalMolang(nextKf.pre, time, entity) : nextKf.pre.vector;
            // Catmull-Rom spline
            if (lastKf.catmullrom) {
                // Missing neighbours repeat the end points
                Vec3 p0 = v1, p1 = v1, p2 = v2, p3 = v2;
                if (lastIdx > 0) {
                    KeyframeValue kv = keyframes.get(lastIdx - 1).post;
                    p0 = kv.isMolang() ? evalMolang(kv, time, entity) : kv.vector;
                }
                if (lastIdx + 1 < keyframes.size() - 1) {
                    KeyframeValue kv = keyframes.get(lastIdx + 2).pre;
                    p3 = kv.isMolang() ? evalMolang(kv, time, entity) : kv.vector;
                }
                float t = alpha, t2 = t * t, t3 = t2 * t;
                return new Vec3(0.5 * ((2 * p1.x) + (-p0.x + p2.x) * t + (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * t2 + (-p0.x + 3 * p1.x - 3 * p2.x + p3.x) * t3),
                        0.5 * ((2 * p1.y) + (-p0.y + p2.y) * t + (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * t2 + (-p0.y + 3 * p1.y - 3 * p2.y + p3.y) * t3),
                        0.5 * ((2 * p1.z) + (-p0.z + p2.z) * t + (2 * p0.z - 5 * p1.z + 4 * p2.z - p3.z) * t2 + (-p0.z + 3 * p1.z - 3 * p2.z + p3.z) * t3));
            }
            // Linear interpolation
            return new Vec3(v1.x + (v2.x - v1.x) * alpha, v1.y + (v2.y - v1.y) * alpha, v1.z + (v2.z - v1.z) * alpha);
        }

        /**
         * Used to evaluate a Molang expression to a vector.
         * A single result is applied to all three axes. An expression that fails to evaluate gives zero.
         */
        private static Vec3 evalMolang(KeyframeValue value, float time, LivingEntity entity) {
            String expr = preprocessMolangQueries(value, time, entity);
            try {
                if (expr.trim().startsWith("[") && expr.trim().endsWith("]")) {
                    String inner = expr.trim().substring(1, expr.trim().length() - 1);
                    String[] parts = inner.split(",");
                    return new Vec3(parts.length > 0 ? evalFloat(parts[0].trim(), time, entity) : 0, parts.length > 1 ? evalFloat(parts[1].trim(), time, entity) : 0, parts.length > 2 ? evalFloat(parts[2].trim(), time, entity) : 0);
                }
                float val = evalFloat(expr, time, entity);
                return new Vec3(val, val, val);
            } catch (Exception e) {
                e.printStackTrace();
                return Vec3.ZERO;
            }
        }

        /**
         * Used to evaluate a scalar Molang expression.
         * It supports numbers, {@code + - * /}, negation and the {@code math.} functions sin, cos, tan (in degrees), abs, sqrt, pow, min, max and clamp.
         * Anything it cannot evaluate, and a division by zero, gives 0.
         */
        private static float evalFloat(String expr, float time, LivingEntity entity) {
            if (expr == null || expr.isEmpty())
                return 0.0f;
            expr = expr.trim().replace(" ", "");
            String lower = expr.toLowerCase();
            // Math functions
            if (lower.startsWith("math.sin(") && lower.endsWith(")")) {
                return (float) Math.sin(Math.toRadians(evalFloat(expr.substring(9, expr.length() - 1), time, entity)));
            }
            if (lower.startsWith("math.cos(") && lower.endsWith(")")) {
                return (float) Math.cos(Math.toRadians(evalFloat(expr.substring(9, expr.length() - 1), time, entity)));
            }
            if (lower.startsWith("math.tan(") && lower.endsWith(")")) {
                return (float) Math.tan(Math.toRadians(evalFloat(expr.substring(9, expr.length() - 1), time, entity)));
            }
            if (lower.startsWith("math.abs(") && lower.endsWith(")")) {
                return Math.abs(evalFloat(expr.substring(9, expr.length() - 1), time, entity));
            }
            if (lower.startsWith("math.sqrt(") && lower.endsWith(")")) {
                return (float) Math.sqrt(evalFloat(expr.substring(10, expr.length() - 1), time, entity));
            }
            if (lower.startsWith("math.pow(") && lower.endsWith(")")) {
                String inner = expr.substring(9, expr.length() - 1);
                int commaPos = findTopLevelComma(inner);
                if (commaPos != -1) {
                    float base = evalFloat(inner.substring(0, commaPos), time, entity);
                    float exp = evalFloat(inner.substring(commaPos + 1), time, entity);
                    return (float) Math.pow(base, exp);
                }
            }
            if (lower.startsWith("math.min(") && lower.endsWith(")")) {
                String inner = expr.substring(9, expr.length() - 1);
                int commaPos = findTopLevelComma(inner);
                if (commaPos != -1) {
                    return Math.min(evalFloat(inner.substring(0, commaPos), time, entity), evalFloat(inner.substring(commaPos + 1), time, entity));
                }
            }
            if (lower.startsWith("math.max(") && lower.endsWith(")")) {
                String inner = expr.substring(9, expr.length() - 1);
                int commaPos = findTopLevelComma(inner);
                if (commaPos != -1) {
                    return Math.max(evalFloat(inner.substring(0, commaPos), time, entity), evalFloat(inner.substring(commaPos + 1), time, entity));
                }
            }
            if (lower.startsWith("math.clamp(") && lower.endsWith(")")) {
                String inner = expr.substring(11, expr.length() - 1);
                List<String> parts = new ArrayList<>();
                int depth = 0;
                int start = 0;
                for (int i = 0; i < inner.length(); i++) {
                    char c = inner.charAt(i);
                    if (c == '(')
                        depth++;
                    else if (c == ')')
                        depth--;
                    else if (c == ',' && depth == 0) {
                        parts.add(inner.substring(start, i));
                        start = i + 1;
                    }
                }
                parts.add(inner.substring(start));
                if (parts.size() == 3) {
                    float val = evalFloat(parts.get(0), time, entity);
                    float min = evalFloat(parts.get(1), time, entity);
                    float max = evalFloat(parts.get(2), time, entity);
                    return Math.max(min, Math.min(max, val));
                }
            }
            // Add and subtract, split at the last operator outside of brackets
            int depth = 0;
            for (int i = expr.length() - 1; i >= 0; i--) {
                char c = expr.charAt(i);
                if (c == ')')
                    depth++;
                else if (c == '(')
                    depth--;
                else if (depth == 0) {
                    if (c == '+') {
                        return evalFloat(expr.substring(0, i), time, entity) + evalFloat(expr.substring(i + 1), time, entity);
                    } else if (c == '-' && i > 0) {
                        // A minus after another operator or an exponent marker is a sign and not a subtraction
                        char prev = expr.charAt(i - 1);
                        boolean isOperator = prev != '+' && prev != '-' && prev != '*' && prev != '/' && prev != '(' && prev != 'E' && prev != 'e';
                        if (isOperator) {
                            return evalFloat(expr.substring(0, i), time, entity) - evalFloat(expr.substring(i + 1), time, entity);
                        }
                    }
                }
            }
            // Multiply and divide
            depth = 0;
            for (int i = expr.length() - 1; i >= 0; i--) {
                char c = expr.charAt(i);
                if (c == ')')
                    depth++;
                else if (c == '(')
                    depth--;
                else if (depth == 0) {
                    if (c == '*') {
                        return evalFloat(expr.substring(0, i), time, entity) * evalFloat(expr.substring(i + 1), time, entity);
                    }
                    if (c == '/') {
                        float denominator = evalFloat(expr.substring(i + 1), time, entity);
                        return denominator == 0 ? 0 : evalFloat(expr.substring(0, i), time, entity) / denominator;
                    }
                }
            }
            // Negation
            if (expr.startsWith("-")) {
                return -evalFloat(expr.substring(1), time, entity);
            }
            // Number
            try {
                return Float.parseFloat(expr);
            } catch (NumberFormatException e) {
                return 0.0f;
            }
        }

        /**
         * Used to replace the Molang queries of the value with their current values for the {@link LivingEntity}.
         */
        private static String preprocessMolangQueries(KeyframeValue value, float time, LivingEntity entity) {
            String resolved = value.molang;
            for (String query : value.queries) resolved = resolved.replace(query, getQueryValue(query, time, entity));
            return resolved;
        }

        /**
         * Used to get the current value of a Molang query as text.
         * Boolean queries give 1.0 or 0.0 and an unknown query is returned unchanged.
         */
        private static String getQueryValue(String query, float time, LivingEntity entity) {
            Minecraft minecraft = Minecraft.getInstance();
            Vec3 movement = entity.getDeltaMovement();
            return switch (query) {
                case "query.anim_time" -> format(time);
                case "query.head_x_rotation" -> format(Mth.wrapDegrees(entity.getXRot()));
                case "query.head_y_rotation" -> format(Mth.wrapDegrees(entity.getYRot()));
                case "query.body_x_rotation" -> format(Mth.wrapDegrees(Mth.lerp(minecraft.getTimer().getGameTimeDeltaPartialTick(false), entity.xRotO, entity.getXRot())));
                case "query.body_y_rotation" -> format(Mth.wrapDegrees(Mth.rotLerp(minecraft.getTimer().getGameTimeDeltaPartialTick(false), entity.yBodyRotO, entity.yBodyRot)));
                case "query.life_time" -> format(entity.tickCount / 20.0F);
                case "query.health" -> format(entity.getHealth());
                case "query.max_health" -> format(entity.getMaxHealth());
                case "query.is_on_ground" -> entity.onGround() ? "1.0" : "0.0";
                case "query.is_in_water" -> entity.isInWater() ? "1.0" : "0.0";
                case "query.is_sneaking" -> entity.isCrouching() ? "1.0" : "0.0";
                case "query.is_sprinting" -> entity.isSprinting() ? "1.0" : "0.0";
                case "query.is_swimming" -> entity.isSwimming() ? "1.0" : "0.0";
                case "query.is_riding" -> entity.isPassenger() ? "1.0" : "0.0";
                case "query.is_sleeping" -> entity.isSleeping() ? "1.0" : "0.0";
                case "query.is_alive" -> entity.isAlive() ? "1.0" : "0.0";
                case "query.is_gliding" -> entity.isFallFlying() ? "1.0" : "0.0";
                case "query.ground_speed" -> format((float) Math.sqrt(movement.x * movement.x + movement.z * movement.z));
                case "query.vertical_speed", "query.position_delta_y" -> format((float) movement.y);
                case "query.speed" -> format((float) movement.length());
                case "query.limb_swing", "query.modified_distance_moved" -> format(entity.walkAnimation.position());
                case "query.limb_swing_amount", "query.modified_move_speed", "query.walk_anim_speed" -> format(entity.walkAnimation.speed());
                case "query.hurt_time" -> format((float) entity.hurtTime);
                case "query.death_time" -> format((float) entity.deathTime);
                case "query.swing_progress" -> format(entity.getAttackAnim(1.0F));
                case "query.is_using_item" -> entity.isUsingItem() ? "1.0" : "0.0";
                case "query.use_item_interval" -> format((float) entity.getUseItemRemainingTicks());
                case "query.is_first_person" -> minecraft.options.getCameraType().isFirstPerson() ? "1.0" : "0.0";
                case "query.main_hand_item_use_duration" -> entity.isUsingItem() && entity.getUsedItemHand() == InteractionHand.MAIN_HAND ? format((float) entity.getUseItemRemainingTicks()) : "0.0";
                case "query.yaw_speed" -> format(Math.abs(Mth.wrapDegrees(entity.getYRot() - entity.yRotO)));
                case "query.position_delta_x" -> format((float) movement.x);
                case "query.position_delta_z" -> format((float) movement.z);
                default -> query;
            };
        }

        /**
         * Used to format a value for a Molang expression with a dot as decimal separator in every locale.
         */
        private static String format(float value) {
            return String.format(Locale.ROOT, "%.6f", value);
        }

        /**
         * Used to get the index of the first comma that is not inside brackets, or -1 if there is none.
         */
        private static int findTopLevelComma(String expr) {
            int depth = 0;
            for (int i = 0; i < expr.length(); i++) {
                char c = expr.charAt(i);
                if (c == '(')
                    depth++;
                else if (c == ')')
                    depth--;
                else if (c == ',' && depth == 0)
                    return i;
            }
            return -1;
        }
    }
}
