package com.simpledi.ioc;

import com.simpledi.exception.BindingNotFoundException;
import com.simpledi.exception.ConstructorAmbiguityException;
import com.simpledi.exception.NoSuitableConstructorException;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class InjectorImpl implements Injector {

    private static final Object lock = new Object();

    private final Map<Class<?>, Implementation> bindings = new LinkedHashMap<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        final Implementation implementation = bindings.get(type);
        if (implementation == null) {
            return null;
        } else if (implementation.isSingleton() && instances.containsKey(type)) {
            return () -> getExistingInstance(type);
        }
        final Map<Class<?>, Constructor<?>> constructors = new LinkedHashMap<>();
        collectAllConstructors(type, constructors);
        return () -> getInstance(type, constructors);
    }

    private void collectAllConstructors(Class<?> type, Map<Class<?>, Constructor<?>> constructors) {
        final Implementation implementation = bindings.get(type);
        if (implementation == null) {
            throw new BindingNotFoundException("No binding detected for " + type.getName());
        }
        final Constructor<?> suitableConstructor = findSuitableConstructor(implementation.getImplClass());
        for (Class<?> paramType : suitableConstructor.getParameterTypes()) {
            collectAllConstructors(paramType, constructors);
        }
        constructors.put(type, suitableConstructor);
    }

    private <T> T getInstance(Class<T> mainType, Map<Class<?>, Constructor<?>> constructors) {
        constructors.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(mainType))
                .forEach(entry -> getInstance(entry.getKey(), entry.getValue()));
        return Optional.ofNullable(constructors.get(mainType))
                .map(mainConstructor -> getInstance(mainType, mainConstructor))
                .orElse(null);
    }

    private <T> T getInstance(Class<T> type, Constructor<?> constructor) {
        final boolean isSingleton = Optional.ofNullable(bindings.get(type))
                .map(Implementation::isSingleton)
                .orElse(false);
        T instance = getExistingInstance(type);
        if (!isSingleton) {
            instance = createInstance(type, constructor);
        } else if (instance == null) {
            synchronized (lock) {
                instance = getExistingInstance(type);
                if (instance == null) {
                    instance = createInstance(type, constructor);
                }
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private <T> T getExistingInstance(Class<T> type) {
        return (T) instances.get(type);
    }

    private <T> T createInstance(Class<T> type, Constructor<?> constructor) {
        try {
            @SuppressWarnings("unchecked")
            T instance = (T) constructor.newInstance(extractInitArgs(constructor));
            instances.put(type, instance);
            return instance;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create an instance of the " + constructor.getDeclaringClass().getName());
        }
    }

    private Object[] extractInitArgs(Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameterTypes())
                .map(instances::get)
                .filter(Objects::nonNull)
                .toArray();
    }

    private Constructor<?> findSuitableConstructor(Class<?> implClass) {
        Constructor<?> constructorWithInject = null;
        Constructor<?> defaultConstructor = null;
        for (Constructor<?> constructor : implClass.getConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                if (constructorWithInject != null) {
                    throw new ConstructorAmbiguityException("More than one @Inject constructor detected in the " + implClass.getName());
                }
                constructorWithInject = constructor;
            } else if (constructor.getParameterTypes().length == 0) {
                defaultConstructor = constructor;
            }
        }
        if (constructorWithInject == null && defaultConstructor == null) {
            throw new NoSuitableConstructorException("No @Inject or default constructor detected in the " + implClass.getName());
        }
        return constructorWithInject != null ? constructorWithInject : defaultConstructor;
    }

    @Override
    public <T> void bind(Class<T> base, Class<? extends T> impl) {
        bindings.put(base, new Implementation(impl));
    }

    @Override
    public <T> void bindSingleton(Class<T> base, Class<? extends T> impl) {
        bindings.put(base, new Implementation(impl, Scope.SINGLETON));
    }

    private static class Implementation {

        private final Class<?> implClass;
        private final Scope scope;

        public Implementation(Class<?> implClass) {
            this.implClass = implClass;
            this.scope = Scope.PROTOTYPE;
        }

        public Implementation(Class<?> implClass, Scope scope) {
            this.implClass = implClass;
            this.scope = scope;
        }

        public Class<?> getImplClass() {
            return implClass;
        }

        public boolean isSingleton() {
            return scope == Scope.SINGLETON;
        }
    }

    private enum Scope {
        SINGLETON, PROTOTYPE
    }
}
