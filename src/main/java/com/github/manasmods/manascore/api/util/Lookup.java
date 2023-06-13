package com.github.manasmods.manascore.api.util;

import com.github.manasmods.manascore.api.client.animation.AnimationPart;
import com.github.manasmods.manascore.client.animation.CompiledAnimationPart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private Map<String, Argument> nameToVar;

    @Getter
    private Object[] arguments;

    public static Lookup create(AnimationPart.RendererConfig config) {
        Lookup lookup = new Lookup();
        lookup.nameToVar = new HashMap<>();
        lookup.arguments = new Object[config.getParams().size()];

        Pattern derivedPattern = Pattern.compile("\\$\\{([^}]+)\\}");

        for(String key : config.getParams().keySet()) {
            AnimationPart.RendererConfig.ConfigValue configValue = config.getParams().get(key);

            if(configValue.getScope().equalsIgnoreCase("provided")) {
                lookup.nameToVar.put(key, new Argument(configValue.getPosition(), ArgumentType.PROVIDED));
                lookup.arguments[configValue.getPosition()] = configValue.getValue();
            } else if(configValue.getScope().equalsIgnoreCase("model")) {

            } else if(configValue.getScope().equalsIgnoreCase("derived")) {
                if(!(configValue.getValue() instanceof String)) {
                    throw new IllegalArgumentException("Derived value must be string");
                }

                String copyArgument = (String)configValue.getValue();

                if(!derivedPattern.matcher(copyArgument).matches()) {
                    throw new IllegalArgumentException("Derived value must be in the format ${VARIABLE_NAME} with VARIABLE_NAME being [a-zA-Z0-9]");
                }

                copyArgument = copyArgument.replace("$", "")
                        .replace("{", "")
                        .replace("}", "");

                lookup.nameToVar.put(key, new Argument(configValue.getPosition(), ArgumentType.DERIVED, copyArgument));
            } else if(configValue.getScope().equalsIgnoreCase("dynamic")) {
                lookup.nameToVar.put(key, new Argument(configValue.getPosition(), ArgumentType.DYNAMIC));
            }
        }

        return lookup;
    }

    @Nullable
    public Argument lookup(String key) {
        if(nameToVar.containsKey(key))
            return this.nameToVar.get(key);
        else
            return null;
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

    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class Argument {
        public final int position;

        public final ArgumentType type;

        public String value;
    }

    public enum ArgumentType {
        PROVIDED, DERIVED, DYNAMIC;
    }

}
