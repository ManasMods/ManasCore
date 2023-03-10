package com.github.manasmods.manascore.api.client.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An animation constitutes a separate part of an animation with its own renderer and shared values
 */
public class AnimationPart {

    /**
     * The internal name of the animation part
     */
    private String name;

    private List<RendererConfig> rendererChain;

    private String abortAnimation;

    public static class RendererConfig {
        /**
         * The name of the renderer to use
         */
        private String name;

        /**
         * Parameters that are automatically passed to the renderer, these will be taken either from the AnimationPart Shared Value Storage or the global Animation storage
         */
        private Map<String, Object> params;
    }

    public static AnimationPart fromJson(JsonObject object) {
        AnimationPart part = new AnimationPart();

        part.name = object.get("name").getAsString();

        if(object.has("abortAnimation")) {
            object.get("abortAnimation").getAsString();
        }

        JsonArray parts = object.getAsJsonArray("rendererChain");

        part.rendererChain = new ArrayList<>();

        for(JsonElement element : parts.asList()) {
            RendererConfig config = new RendererConfig();
            config.name = element.getAsJsonObject().get("name").getAsString();
            config.params = new HashMap<>();

            Map<String, JsonElement> paramsMap = element.getAsJsonObject().get("params").getAsJsonObject().asMap();

            for(String paramName : paramsMap.keySet()) {
                JsonElement param = paramsMap.get(paramName);

                if(param.isJsonPrimitive()) {
                    if(param.getAsJsonPrimitive().isString()) {
                        config.params.put(paramName, param.getAsString());
                    }

                    if(param.getAsJsonPrimitive().isBoolean()) {
                        config.params.put(paramName, param.getAsBoolean());
                    }

                    if(param.getAsJsonPrimitive().isNumber()) {
                        config.params.put(paramName, param.getAsNumber());
                    }
                }
            }

            part.rendererChain.add(config);
        }

        return part;
    }

}
