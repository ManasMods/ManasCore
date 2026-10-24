/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.network.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.architectury.event.EventFactory;
import org.spongepowered.asm.mixin.Mixin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mixin(EventFactory.class)
public class MixinEventFactory {

    @WrapMethod(method = "invokeMethod", remap = false)
    private static Object invokeListener(Object listener, Method method, Object[] args, Operation<Object> original) throws Throwable {
        try {
            return method.invoke(listener, args);
        } catch (IllegalAccessException exception) {
            return original.call(listener, method, args);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }
}
