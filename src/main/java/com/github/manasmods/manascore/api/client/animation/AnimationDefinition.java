package com.github.manasmods.manascore.api.client.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * An animation is a semi-entity which renders effects on-screen of every player.
 */
public class AnimationDefinition {

    /**
     * The name of the animation
     */
    private String name;

    /**
     * Whether the animation is cancelable, cancelable animations may call cancel animations
     */
    private boolean cancelable = false;

    /**
     * The duration of the animation after which it will be automatically canceled, set to 0 for customized end.
     */
    private int durationTicks = 0;

    /**
     * The parts the animation is comprised of
     */
    private List<AnimationPart> parts;

    public static AnimationDefinition fromJson(JsonObject object) {
        AnimationDefinition definition = new AnimationDefinition();

        definition.name = GsonHelper.getAsString(object, "name");

        if(object.has("durationTicks")) {
            definition.durationTicks = GsonHelper.getAsInt(object, "durationTicks");
        }

        if(object.has("cancelable")) {
            definition.cancelable = GsonHelper.getAsBoolean(object, "cancelable");
        }

        JsonArray parts = GsonHelper.getAsJsonArray(object, "parts");

        definition.parts = new ArrayList<>();

        try {
            for(JsonElement element : parts.asList()) {
                AnimationPart part = AnimationPart.fromJson(element.getAsJsonObject());

                definition.parts.add(part);
            }
        } catch (IllegalStateException ex) {
            LogUtils.getLogger().error("Expected object, got something else in parts");
            ex.printStackTrace();
        }

        return definition;
    }

}
