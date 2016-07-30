package io.gunmetal.function;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * @author rees.byars
 */
public class MethodHandleFuncFactory extends CachingFuncFactory {

    @Override
    FuncAny getFuncAny(Method method) {

        Class<?> declaringClass = method.getDeclaringClass();

        MethodHandles.Lookup lookup = getLookup(declaringClass);

        try {

            MethodHandle methodHandle = lookup.unreflectSpecial(method, declaringClass);

            return (target, args) -> {
                try {
                    return methodHandle
                            .bindTo(target)
                            .invokeWithArguments(args);
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            };

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

}
