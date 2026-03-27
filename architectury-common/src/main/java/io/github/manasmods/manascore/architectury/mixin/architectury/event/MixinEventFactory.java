package io.github.manasmods.manascore.architectury.mixin.architectury.event;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Mixin(targets = "dev.architectury.event.EventFactory$EventImpl")
public class MixinEventFactory<T> {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @WrapMethod( method = "register", remap = false )
    void registerListener(T listener, Operation<Void> original) {
        readWriteLock.writeLock().lock();
        original.call(listener);
        readWriteLock.writeLock().unlock();
    }

    @WrapMethod( method = "unregister", remap = false )
    void removeListener(T listener, Operation<Void> original) {
        readWriteLock.writeLock().lock();
        original.call(listener);
        readWriteLock.writeLock().unlock();
    }

    @WrapMethod( method = "isRegistered", remap = false )
    boolean isListenerRegistered(T listener, Operation<Boolean> original) {
        readWriteLock.readLock().lock();
        boolean result = original.call(listener);
        readWriteLock.readLock().unlock();
        return result;
    }

    @WrapMethod( method = "clearListeners", remap = false )
    void clearListeners(Operation<Void> original) {
        readWriteLock.writeLock().lock();
        original.call();
        readWriteLock.writeLock().unlock();
    }

    @WrapMethod( method = "update", remap = false )
    void updateInvoker(Operation<Void> original) {
        readWriteLock.readLock().lock();
        original.call();
        readWriteLock.readLock().unlock();
    }
}
