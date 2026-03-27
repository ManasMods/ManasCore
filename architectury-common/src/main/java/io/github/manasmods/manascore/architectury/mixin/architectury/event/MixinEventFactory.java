package io.github.manasmods.manascore.architectury.mixin.architectury.event;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.AbstractQueue;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

@Mixin(targets = "dev.architectury.event.EventFactory$EventImpl")
public class MixinEventFactory<T> {
    @Shadow(remap = false) @Final
    private Function<List<T>, T> function;
    @Shadow(remap = false)
    private T invoker;
    private AbstractQueue<T> listenerQueue;

    @Inject(
            method = "<init>", at = @At("RETURN"),
            remap = false
    )
    void initQueue(Function<List<T>, T> function, CallbackInfo ci) {
        listenerQueue = new ConcurrentLinkedQueue<>();
    }

    @Inject(
            method = "register", at = @At("HEAD"),
            remap = false, cancellable = true
    )
    void registerListener(T listener, CallbackInfo ci) {
        listenerQueue.add(listener);
        invoker = null;
        ci.cancel();
    }

    @Inject(
            method = "unregister", at = @At("HEAD"),
            remap = false, cancellable = true
    )
    void removeListener(T listener, CallbackInfo ci) {
        listenerQueue.remove(listener);
        invoker = null;
        ci.cancel();
    }

    @Inject(
            method = "isRegistered", at = @At("HEAD"),
            remap = false, cancellable = true
    )
    void isListenerRegistered(T listener, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(listenerQueue.contains(listener));
    }

    @Inject(
            method = "clearListeners", at = @At("HEAD"),
            remap = false, cancellable = true
    )
    void clearListeners(CallbackInfo ci) {
        listenerQueue.clear();
        invoker = null;
        ci.cancel();
    }

    @Inject(
            method = "update", at = @At("HEAD"),
            remap = false, cancellable = true
    )
    void updateInvoker(CallbackInfo ci) {
        if (listenerQueue.size() == 1) {
            invoker = listenerQueue.peek();
        } else {
            invoker = function.apply(listenerQueue.stream().toList());
        }
        ci.cancel();
    }
}
