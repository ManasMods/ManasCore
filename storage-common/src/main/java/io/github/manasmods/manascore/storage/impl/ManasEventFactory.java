package io.github.manasmods.manascore.storage.impl;

import com.google.common.reflect.AbstractInvocationHandler;
import dev.architectury.annotations.ForgeEvent;
import dev.architectury.annotations.ForgeEventCancellable;
import dev.architectury.event.*;
import dev.architectury.injectables.annotations.ExpectPlatform;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.AbstractQueue;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ManasEventFactory {
    private ManasEventFactory() {}
    public static <T> Event<T> of(Function<List<T>, T> function) {
        return new EventImpl<>(function);
    }

    @SafeVarargs
    public static <T> Event<T> createLoop(T... typeGetter) {
        if (typeGetter.length != 0) throw new IllegalStateException("array must be empty!");
        return createLoop((Class<T>) typeGetter.getClass().getComponentType());
    }

    private static <T, R> R invokeMethod(T listener, Method method, Object[] args) throws Throwable {
        return (R) MethodHandles.lookup().unreflect(method)
                .bindTo(listener).invokeWithArguments(args);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> Event<T> createLoop(Class<T> clazz) {
        return of(listeners -> (T) Proxy.newProxyInstance(EventFactory.class.getClassLoader(), new Class[]{clazz}, new AbstractInvocationHandler() {
            @Override
            protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
                for (var listener : listeners) {
                    invokeMethod(listener, method, args);
                }
                return null;
            }
        }));
    }

    @SafeVarargs
    public static <T> Event<T> createEventResult(T... typeGetter) {
        if (typeGetter.length != 0) throw new IllegalStateException("array must be empty!");
        return createEventResult((Class<T>) typeGetter.getClass().getComponentType());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> Event<T> createEventResult(Class<T> clazz) {
        return of(listeners -> (T) Proxy.newProxyInstance(EventFactory.class.getClassLoader(), new Class[]{clazz}, new AbstractInvocationHandler() {
            @Override
            protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
                for (var listener : listeners) {
                    var result = (EventResult) Objects.requireNonNull(invokeMethod(listener, method, args));
                    if (result.interruptsFurtherEvaluation()) {
                        return result;
                    }
                }
                return EventResult.pass();
            }
        }));
    }

    @SafeVarargs
    public static <T> Event<T> createCompoundEventResult(T... typeGetter) {
        if (typeGetter.length != 0) throw new IllegalStateException("array must be empty!");
        return createCompoundEventResult((Class<T>) typeGetter.getClass().getComponentType());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> Event<T> createCompoundEventResult(Class<T> clazz) {
        return of(listeners -> (T) Proxy.newProxyInstance(EventFactory.class.getClassLoader(), new Class[]{clazz}, new AbstractInvocationHandler() {
            @Override
            protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
                for (var listener : listeners) {
                    var result = (CompoundEventResult) Objects.requireNonNull(invokeMethod(listener, method, args));
                    if (result.interruptsFurtherEvaluation()) {
                        return result;
                    }
                }
                return CompoundEventResult.pass();
            }
        }));
    }

    @SafeVarargs
    public static <T> Event<Consumer<T>> createConsumerLoop(T... typeGetter) {
        if (typeGetter.length != 0) throw new IllegalStateException("array must be empty!");
        return createConsumerLoop((Class<T>) typeGetter.getClass().getComponentType());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> Event<Consumer<T>> createConsumerLoop(Class<T> clazz) {
        Event<Consumer<T>> event = of(listeners -> (Consumer<T>) Proxy.newProxyInstance(EventFactory.class.getClassLoader(), new Class[]{Consumer.class}, new AbstractInvocationHandler() {
            @Override
            protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
                for (var listener : listeners) {
                    invokeMethod(listener, method, args);
                }
                return null;
            }
        }));
        Class<?> superClass = clazz;
        do {
            if (superClass.isAnnotationPresent(ForgeEvent.class)) {
                return attachToForge(event);
            }
            superClass = superClass.getSuperclass();
        } while (superClass != null);
        return event;
    }

    @SafeVarargs
    public static <T> Event<EventActor<T>> createEventActorLoop(T... typeGetter) {
        if (typeGetter.length != 0) throw new IllegalStateException("array must be empty!");
        return createEventActorLoop((Class<T>) typeGetter.getClass().getComponentType());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> Event<EventActor<T>> createEventActorLoop(Class<T> clazz) {
        Event<EventActor<T>> event = of(listeners -> (EventActor<T>) Proxy.newProxyInstance(EventFactory.class.getClassLoader(), new Class[]{EventActor.class}, new AbstractInvocationHandler() {
            @Override
            protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
                for (var listener : listeners) {
                    var result = (EventResult) invokeMethod(listener, method, args);
                    if (result.interruptsFurtherEvaluation()) {
                        return result;
                    }
                }
                return EventResult.pass();
            }
        }));
        Class<?> superClass = clazz;
        do {

            if (superClass.isAnnotationPresent(ForgeEventCancellable.class)) {
                return attachToForgeEventActorCancellable(event);
            }
            superClass = superClass.getSuperclass();
        } while (superClass != null);
        superClass = clazz;
        do {

            if (superClass.isAnnotationPresent(ForgeEvent.class)) {
                return attachToForgeEventActor(event);
            }
            superClass = superClass.getSuperclass();
        } while (superClass != null);
        return event;
    }

    @ExpectPlatform
    @ApiStatus.Internal
    public static <T> Event<Consumer<T>> attachToForge(Event<Consumer<T>> event) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @ApiStatus.Internal
    public static <T> Event<EventActor<T>> attachToForgeEventActor(Event<EventActor<T>> event) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @ApiStatus.Internal
    public static <T> Event<EventActor<T>> attachToForgeEventActorCancellable(Event<EventActor<T>> event) {
        throw new AssertionError();
    }

    private static class EventImpl<T> implements Event<T> {
        private final Function<List<T>, T> function;
        private T invoker = null;
        private AbstractQueue<T> listeners;

        public EventImpl(Function<List<T>, T> function) {
            this.function = function;
            // We're using ConcurrentLinkedQueue as a thread safe way to add listeners to the event,
            // we're not actually using it as a queue, but as a thread safe linked list.
            this.listeners = new ConcurrentLinkedQueue<>();
        }

        @Override
        public T invoker() {
            if (invoker == null) {
                update();
            }
            return invoker;
        }

        @Override
        public void register(T listener) {
            listeners.add(listener);
            invoker = null;
        }

        @Override
        public void unregister(T listener) {
            listeners.remove(listener);
            invoker = null;
        }

        @Override
        public boolean isRegistered(T listener) {
            return listeners.contains(listener);
        }

        @Override
        public void clearListeners() {
            listeners.clear();
            invoker = null;
        }

        public void update() {
            // Using peek() and .stream().toList() to prevent listeners from being de-registered
            if (listeners.size() == 1) {
                invoker = listeners.peek();
            } else {
                invoker = function.apply(listeners.stream().toList());
            }
        }
    }
}
