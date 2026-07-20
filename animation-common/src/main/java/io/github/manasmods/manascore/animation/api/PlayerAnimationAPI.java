/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
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
import java.util.Map;
import java.util.Set;

/**
 * Bedrock-format player animation runtime.
 * Loader-agnostic: animation JSONs are discovered via the client {@link ResourceManager}
 * from every mod's {@code assets/<modid>/manas_animations/} directory and keyed as
 * {@code <modid>:<animation_name>}. Loaded through the resource manager (not disk walking)
 * so it works across all Architectury modules in both dev and production.
 */
public class PlayerAnimationAPI {
    public static final Logger LOG = LoggerFactory.getLogger("ManasCore Animation");
    public static final Map<String, PlayerAnimation> animations = new Object2ObjectOpenHashMap<>();
    public static final Map<Player, PlayerAnimation> active_animations = new Object2ObjectOpenHashMap<>();

    /** Client-side per-player playback state. */
    public static final Map<Player, PlayerAnimationState> states = new Object2ObjectOpenHashMap<>();

    /** Set while rendering a player into a GUI (inventory/overlay). First-person handling must be
     *  skipped here so the widget shows the whole model instead of only the arms. */
    public static boolean renderingGuiEntity = false;

    public static PlayerAnimationState state(Player player) {
        return states.computeIfAbsent(player, key -> new PlayerAnimationState());
    }

    public static class PlayerAnimationState {
        public String currentAnimation = "";
        public String nextAnimation = "";
        public String currentConditional = "";
        public boolean override = false;
        public boolean firstPerson = false;
        public boolean reset = false;
        public boolean hasProgress = false;
        public float progress = 0f;
        public float lastTickTime = 0f;
        public float lastAnimationProgress = 0f;
        public final Set<Float> playedSounds = new HashSet<>();
    }

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

    public static class PlayerAnimation {
        public final float length;
        public boolean loop = false;
        public boolean hold_on_last_frame = false;
        public final Map<String, PlayerBone> bones;
        public final Map<Float, String> soundEffects;

        public PlayerAnimation(JsonObject animation) {
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
            this.bones = new HashMap<>();
            if (animation.has("bones")) {
                JsonObject bonesObj = animation.getAsJsonObject("bones");
                for (String boneName : bonesObj.keySet()) {
                    this.bones.put(boneName, new PlayerBone(bonesObj.getAsJsonObject(boneName)));
                }
            }
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

    public static class PlayerBone {
        public final List<Keyframe> rotations;
        public final List<Keyframe> positions;
        public final List<Keyframe> scales;

        public PlayerBone(JsonObject bone) {
            this.rotations = parseTransform(bone, "rotation");
            this.positions = parseTransform(bone, "position");
            this.scales = parseTransform(bone, "scale");
        }

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

        public static class KeyframeValue {
            public final Vec3 vector;
            public final String molang;

            public KeyframeValue(Vec3 vector) {
                this.vector = vector;
                this.molang = null;
            }

            public KeyframeValue(String molang) {
                this.molang = molang;
                this.vector = null;
            }

            public boolean isMolang() {
                return molang != null;
            }
        }

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

        public static Vec3 interpolate(List<Keyframe> keyframes, float time, Player player) {
            if (keyframes.isEmpty())
                return null;
            if (keyframes.size() == 1) {
                Keyframe kf = keyframes.get(0);
                return kf.value.isMolang() ? evalMolang(kf.value.molang, time, player) : kf.value.vector;
            }
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
            Vec3 postVec = lastKf.post.isMolang() ? evalMolang(lastKf.post.molang, time, player) : lastKf.post.vector;
            if (nextKf == null)
                return postVec;
            float t1 = lastKf.time;
            float t2_ = nextKf.time;
            if (t1 == t2_)
                return postVec;
            float alpha = (time - t1) / (t2_ - t1);
            Vec3 v1 = postVec;
            Vec3 v2 = nextKf.pre.isMolang() ? evalMolang(nextKf.pre.molang, time, player) : nextKf.pre.vector;
            if (lastKf.catmullrom) {
                Vec3 p0 = v1, p1 = v1, p2 = v2, p3 = v2;
                if (lastIdx > 0) {
                    KeyframeValue kv = keyframes.get(lastIdx - 1).post;
                    p0 = kv.isMolang() ? evalMolang(kv.molang, time, player) : kv.vector;
                }
                if (lastIdx + 1 < keyframes.size() - 1) {
                    KeyframeValue kv = keyframes.get(lastIdx + 2).pre;
                    p3 = kv.isMolang() ? evalMolang(kv.molang, time, player) : kv.vector;
                }
                float t = alpha, t2 = t * t, t3 = t2 * t;
                return new Vec3(0.5 * ((2 * p1.x) + (-p0.x + p2.x) * t + (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * t2 + (-p0.x + 3 * p1.x - 3 * p2.x + p3.x) * t3),
                        0.5 * ((2 * p1.y) + (-p0.y + p2.y) * t + (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * t2 + (-p0.y + 3 * p1.y - 3 * p2.y + p3.y) * t3),
                        0.5 * ((2 * p1.z) + (-p0.z + p2.z) * t + (2 * p0.z - 5 * p1.z + 4 * p2.z - p3.z) * t2 + (-p0.z + 3 * p1.z - 3 * p2.z + p3.z) * t3));
            }
            return new Vec3(v1.x + (v2.x - v1.x) * alpha, v1.y + (v2.y - v1.y) * alpha, v1.z + (v2.z - v1.z) * alpha);
        }

        private static Vec3 evalMolang(String expr, float time, Player player) {
            expr = preprocessMolangQueries(expr, time, player);
            try {
                if (expr.trim().startsWith("[") && expr.trim().endsWith("]")) {
                    String inner = expr.trim().substring(1, expr.trim().length() - 1);
                    String[] parts = inner.split(",");
                    return new Vec3(parts.length > 0 ? evalFloat(parts[0].trim(), time, player) : 0, parts.length > 1 ? evalFloat(parts[1].trim(), time, player) : 0, parts.length > 2 ? evalFloat(parts[2].trim(), time, player) : 0);
                }
                float val = evalFloat(expr, time, player);
                return new Vec3(val, val, val);
            } catch (Exception e) {
                e.printStackTrace();
                return Vec3.ZERO;
            }
        }

        private static float evalFloat(String expr, float time, Player player) {
            if (expr == null || expr.isEmpty())
                return 0.0f;
            expr = expr.trim().replace(" ", "");
            String lower = expr.toLowerCase();
            if (lower.startsWith("math.sin(") && lower.endsWith(")")) {
                return (float) Math.sin(Math.toRadians(evalFloat(expr.substring(9, expr.length() - 1), time, player)));
            }
            if (lower.startsWith("math.cos(") && lower.endsWith(")")) {
                return (float) Math.cos(Math.toRadians(evalFloat(expr.substring(9, expr.length() - 1), time, player)));
            }
            if (lower.startsWith("math.tan(") && lower.endsWith(")")) {
                return (float) Math.tan(Math.toRadians(evalFloat(expr.substring(9, expr.length() - 1), time, player)));
            }
            if (lower.startsWith("math.abs(") && lower.endsWith(")")) {
                return Math.abs(evalFloat(expr.substring(9, expr.length() - 1), time, player));
            }
            if (lower.startsWith("math.sqrt(") && lower.endsWith(")")) {
                return (float) Math.sqrt(evalFloat(expr.substring(10, expr.length() - 1), time, player));
            }
            if (lower.startsWith("math.pow(") && lower.endsWith(")")) {
                String inner = expr.substring(9, expr.length() - 1);
                int commaPos = findTopLevelComma(inner);
                if (commaPos != -1) {
                    float base = evalFloat(inner.substring(0, commaPos), time, player);
                    float exp = evalFloat(inner.substring(commaPos + 1), time, player);
                    return (float) Math.pow(base, exp);
                }
            }
            if (lower.startsWith("math.min(") && lower.endsWith(")")) {
                String inner = expr.substring(9, expr.length() - 1);
                int commaPos = findTopLevelComma(inner);
                if (commaPos != -1) {
                    return Math.min(evalFloat(inner.substring(0, commaPos), time, player), evalFloat(inner.substring(commaPos + 1), time, player));
                }
            }
            if (lower.startsWith("math.max(") && lower.endsWith(")")) {
                String inner = expr.substring(9, expr.length() - 1);
                int commaPos = findTopLevelComma(inner);
                if (commaPos != -1) {
                    return Math.max(evalFloat(inner.substring(0, commaPos), time, player), evalFloat(inner.substring(commaPos + 1), time, player));
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
                    float val = evalFloat(parts.get(0), time, player);
                    float min = evalFloat(parts.get(1), time, player);
                    float max = evalFloat(parts.get(2), time, player);
                    return Math.max(min, Math.min(max, val));
                }
            }
            int depth = 0;
            for (int i = expr.length() - 1; i >= 0; i--) {
                char c = expr.charAt(i);
                if (c == ')')
                    depth++;
                else if (c == '(')
                    depth--;
                else if (depth == 0) {
                    if (c == '+') {
                        return evalFloat(expr.substring(0, i), time, player) + evalFloat(expr.substring(i + 1), time, player);
                    } else if (c == '-' && i > 0) {
                        char prev = expr.charAt(i - 1);
                        boolean isOperator = prev != '+' && prev != '-' && prev != '*' && prev != '/' && prev != '(' && prev != 'E' && prev != 'e';
                        if (isOperator) {
                            return evalFloat(expr.substring(0, i), time, player) - evalFloat(expr.substring(i + 1), time, player);
                        }
                    }
                }
            }
            depth = 0;
            for (int i = expr.length() - 1; i >= 0; i--) {
                char c = expr.charAt(i);
                if (c == ')')
                    depth++;
                else if (c == '(')
                    depth--;
                else if (depth == 0) {
                    if (c == '*') {
                        return evalFloat(expr.substring(0, i), time, player) * evalFloat(expr.substring(i + 1), time, player);
                    }
                    if (c == '/') {
                        float denominator = evalFloat(expr.substring(i + 1), time, player);
                        return denominator == 0 ? 0 : evalFloat(expr.substring(0, i), time, player) / denominator;
                    }
                }
            }
            if (expr.startsWith("-")) {
                return -evalFloat(expr.substring(1), time, player);
            }
            try {
                return Float.parseFloat(expr);
            } catch (NumberFormatException e) {
                return 0.0f;
            }
        }

        private static String preprocessMolangQueries(String expr, float time, Player player) {
            java.util.function.Function<Float, String> fmt = (val) -> String.format(java.util.Locale.ROOT, "%.6f", val);
            Minecraft mc = Minecraft.getInstance();
            return expr.replace("query.anim_time", fmt.apply(time)).replace("query.head_x_rotation", fmt.apply(Mth.wrapDegrees(player.getXRot()))).replace("query.head_y_rotation", fmt.apply(Mth.wrapDegrees(player.getYRot())))
                    .replace("query.body_x_rotation", fmt.apply(Mth.wrapDegrees(Mth.lerp(mc.getTimer().getGameTimeDeltaPartialTick(false), player.xRotO, player.getXRot()))))
                    .replace("query.body_y_rotation", fmt.apply(Mth.wrapDegrees(Mth.rotLerp(mc.getTimer().getGameTimeDeltaPartialTick(false), player.yBodyRotO, player.yBodyRot)))).replace("query.life_time", fmt.apply(player.tickCount / 20.0f))
                    .replace("query.health", fmt.apply(player.getHealth())).replace("query.max_health", fmt.apply(player.getMaxHealth())).replace("query.is_on_ground", player.onGround() ? "1.0" : "0.0")
                    .replace("query.is_in_water", player.isInWater() ? "1.0" : "0.0").replace("query.is_sneaking", player.isCrouching() ? "1.0" : "0.0").replace("query.is_sprinting", player.isSprinting() ? "1.0" : "0.0")
                    .replace("query.is_swimming", player.isSwimming() ? "1.0" : "0.0").replace("query.is_riding", player.isPassenger() ? "1.0" : "0.0").replace("query.is_sleeping", player.isSleeping() ? "1.0" : "0.0")
                    .replace("query.is_alive", player.isAlive() ? "1.0" : "0.0").replace("query.is_gliding", player.isFallFlying() ? "1.0" : "0.0")
                    .replace("query.ground_speed", fmt.apply((float) Math.sqrt(player.getDeltaMovement().x * player.getDeltaMovement().x + player.getDeltaMovement().z * player.getDeltaMovement().z)))
                    .replace("query.vertical_speed", fmt.apply((float) player.getDeltaMovement().y)).replace("query.speed", fmt.apply((float) player.getDeltaMovement().length())).replace("query.limb_swing", fmt.apply(player.walkAnimation.position()))
                    .replace("query.limb_swing_amount", fmt.apply(player.walkAnimation.speed())).replace("query.modified_move_speed", fmt.apply(player.walkAnimation.speed())).replace("query.walk_anim_speed", fmt.apply(player.walkAnimation.speed()))
                    .replace("query.modified_distance_moved", fmt.apply(player.walkAnimation.position())).replace("query.hurt_time", fmt.apply((float) player.hurtTime)).replace("query.death_time", fmt.apply((float) player.deathTime))
                    .replace("query.swing_progress", fmt.apply(player.getAttackAnim(1.0f))).replace("query.is_using_item", player.isUsingItem() ? "1.0" : "0.0").replace("query.use_item_interval", fmt.apply((float) player.getUseItemRemainingTicks()))
                    .replace("query.is_first_person", mc.options.getCameraType().isFirstPerson() ? "1.0" : "0.0")
                    .replace("query.main_hand_item_use_duration", player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND ? fmt.apply((float) player.getUseItemRemainingTicks()) : "0.0")
                    .replace("query.yaw_speed", fmt.apply(Math.abs(Mth.wrapDegrees(player.getYRot() - player.yRotO)))).replace("query.position_delta_x", fmt.apply((float) player.getDeltaMovement().x))
                    .replace("query.position_delta_y", fmt.apply((float) player.getDeltaMovement().y)).replace("query.position_delta_z", fmt.apply((float) player.getDeltaMovement().z));
        }

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
