package io.gunmetal.function;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author rees.byars
 */
abstract class CachingFuncFactory implements FuncFactory {

    private final ConcurrentMap<Method, FuncAny> funcAnyConcurrentMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, MethodHandles.Lookup> lookupCache = new ConcurrentHashMap<>();
    private static final Constructor lookupConstructor;

    static {
        try {
            lookupConstructor =
                    MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        lookupConstructor.setAccessible(true);
    }

    CachingFuncFactory() { }

    @Override
    public FuncAny createFor(Method method) {
        return funcAnyConcurrentMap.computeIfAbsent(method, this::getFuncAny);
    }

    abstract FuncAny getFuncAny(Method method);

    final MethodHandles.Lookup getLookup(Class<?> declaringClass) {
        return lookupCache.computeIfAbsent(declaringClass, this::_getLookup);
    }

    private MethodHandles.Lookup _getLookup(Class<?> declaringClass) {
        try {
            return (MethodHandles.Lookup) lookupConstructor.newInstance(
                    declaringClass,
                    (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
