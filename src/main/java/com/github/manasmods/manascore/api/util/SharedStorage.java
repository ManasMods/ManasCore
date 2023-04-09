package com.github.manasmods.manascore.api.util;

import java.util.Map;
import java.util.HashMap;

public class SharedStorage extends HashMap<String, Object> {

    private Map<String, ImportanceLevel> importanceLevelMap;

    public void putWithImportance(String name, Object value, ImportanceLevel level) {
        this.put(name, value);

        this.importanceLevelMap.put(name, level);
    }

    @Override
    public boolean remove(Object key, Object value) {
        this.importanceLevelMap.remove(key, value);

        return super.remove(key, value);
    }

    @Override
    public Object remove(Object key) {
        this.importanceLevelMap.remove(key);

        return super.remove(key);
    }

    public ImportanceLevel getLevel(String key) {
        if(this.importanceLevelMap.containsKey(key))
            this.importanceLevelMap.get(key);

        return ImportanceLevel.LAST;
    }

    public String getAsString(String name) {
        return (String) this.get(name);
    }

    public int getAsInt(String name) {
        return (int) this.get(name);
    }

    public boolean getAsBool(String name) {
        return (boolean) this.get(name);
    }

    public float getAsFloat(String name) {
        return (float) this.get(name);
    }

    public double getAsDouble(String name) {
        return (double) this.get(name);
    }

    /**
     * Importance level determines the importance of a variable to the caller
     */
    public enum ImportanceLevel {
        FIRST, LAST;
    }

}
