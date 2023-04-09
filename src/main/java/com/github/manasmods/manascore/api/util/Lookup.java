package com.github.manasmods.manascore.api.util;

import com.github.manasmods.manascore.api.client.animation.AnimationPart;
import com.github.manasmods.manascore.client.animation.CompiledAnimationPart;
import lombok.Getter;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Fast storage container for rendering
 * Uses HashMap to save variables once per tick.
 * Variables will be passed to Renderers based on index.
 */
public class Lookup {

    /**
     * Provides lookup functionality for name to variable
     */
    @Getter
    private Map<String, Integer> nameToVar;

    @Getter
    private Object[] arguments;

    public static Lookup create(AnimationPart.RendererConfig config) {
        Lookup lookup = new Lookup();
        lookup.nameToVar = new HashMap<>();
        lookup.arguments = new Object[config.getParams().size()];

        for(String key : config.getParams().keySet()) {
            AnimationPart.RendererConfig.ConfigValue configValue = config.getParams().get(key);
            lookup.nameToVar.put(key, configValue.getPosition());

            if(configValue.getScope().equalsIgnoreCase("provided")) {
                lookup.arguments[configValue.getPosition()] = configValue.getValue();
            }
        }

        return lookup;
    }

    public int lookup(String key) {
        if(nameToVar.containsKey(key))
            return this.nameToVar.get(key);
        else
            return -1;
    }

    public Object getArgument(int i) {
        return arguments[i];
    }

    public void setArgument(int i, Object value) {
        this.arguments[i] = value;
    }

    public int getLength() {
        return arguments.length;
    }

}
