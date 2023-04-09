package com.github.manasmods.manascore.client.animation;

import com.github.manasmods.manascore.api.client.animation.AnimationPart;
import com.github.manasmods.manascore.api.util.SharedStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.EntityRenderersEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Getter
public class RunningAnimation {

    private CompiledAnimation animation;

    /**
     * Global variable storage. Use only when you want to share values across all parts.
     */
    //private SharedStorage globalStorage;

    /**
     * A map of all storages by animationparts
     */
    private Map<String, SharedStorage> partStorage;

    public RunningAnimation(CompiledAnimation animation) {
        this.animation = animation;

        //this.globalStorage = new SharedStorage();
        this.partStorage = new HashMap<>();

        for(CompiledAnimationPart part : animation.getAnimationParts()) {
            SharedStorage storage = this.partStorage.put(part.getName(), new SharedStorage());
        }
    }

    public void preTick(Entity entityIn) {

    }

    //Run post tick to update arguments
    //Only in client!!
    public void postTick() {
        for(CompiledAnimationPart part : this.getAnimation().getAnimationParts()) {
            if(partStorage.containsKey(part.getName())) {
                SharedStorage storage = partStorage.get(part.getName());

                for(CompiledAnimationPart.CompiledRendererConfig renderer : part.getRendererChain()) {
                    for(String key : renderer.getLookup().getNameToVar().keySet()) {
                        if(storage.containsKey(key)) {
                            int i = renderer.getLookup().lookup(key);

                            if(i == -1)
                                throw new RuntimeException(String.format("Renderer argument %s at Renderer %s in AnimationPart %s in Animation %s has no index!", key, renderer.getName(), part.getName(), animation.getName()));

                            renderer.getLookup().setArgument(i, storage.get(key));
                        }
                    }
                }
            }
        }
    }

    public void renderAnimationPart(PoseStack poseStack, MultiBufferSource src, CompiledAnimationPart part) {
        for(CompiledAnimationPart.CompiledRendererConfig renderer : part.getRendererChain()) {
            try {
                renderer.getCachedRenderMethod().invoke(renderer.getRenderer(), poseStack, src, this, renderer.getLookup().getArguments());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Couldn't call render method, did you miss some arguments?", e);
            }
        }
    }

    public void renderAnimation(PoseStack poseStack, MultiBufferSource src) {
        for(CompiledAnimationPart part : this.getAnimation().getAnimationParts()) {
            this.renderAnimationPart(poseStack, src, part);
        }
    }

    /*public void storeGlobal(String key, Object value) {
        this.globalStorage.putWithImportance(key, value, SharedStorage.ImportanceLevel.LAST);
    }

    public void storeGlobalImportant(String key, Object value) {
        this.globalStorage.putWithImportance(key, value, SharedStorage.ImportanceLevel.FIRST);
    }*/

    public boolean storePart(String part, String key, Object value) {
        if(this.partStorage.containsKey(part)) {
            SharedStorage storage = this.partStorage.get(part);

            storage.putWithImportance(key, value, SharedStorage.ImportanceLevel.LAST);
            return true;
        }

        return false;
    }

    /**
     * Gets the variable from global storage if it has a high importance else from part storage
     * @param part the part storage to lookup
     * @param key the key the key of the variable
     * @return the variable either from global or part storage
     */
    /*public Object getVar(String part, String key) {
        if(this.partStorage.containsKey(part)) {
            SharedStorage storage = this.partStorage.get(part);

            if(this.globalStorage.containsKey(key) && this.globalStorage.getLevel(key) == SharedStorage.ImportanceLevel.FIRST) {
                return this.globalStorage.get(key);
            } else {
                return storage.get(key);
            }
        } else {
            return this.globalStorage.get(key);
        }
    }*/

    /**
     * Whether the variable is stored in global or part storage
     * @param part the part to lookup
     * @param key the key
     * @return if the variable is stored somewhere
     */
    /*public boolean hasKey(String part, String key) {
        return this.globalStorage.containsKey(key) || (this.partStorage.containsKey(part) && this.partStorage.get(part).containsKey(key));
    }*/

    public void sync() {

    }

    public static RunningAnimation create(CompiledAnimation animation) {
        return new RunningAnimation(animation);
    }

    public static RunningAnimation createAndRun(CompiledAnimation animation) {
        return null;
    }

}
