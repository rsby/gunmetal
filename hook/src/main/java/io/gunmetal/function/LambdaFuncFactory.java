package io.gunmetal.function;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * @author rees.byars
 */
public class LambdaFuncFactory extends CachingFuncFactory {

    @Override
    FuncAny getFuncAny(Method method) {

        Class<?> declaringClass = method.getDeclaringClass();

        MethodHandles.Lookup lookup = getLookup(declaringClass);

        try {

            MethodHandle targetMethod = lookup.unreflectSpecial(method, declaringClass);

            if (method.getReturnType().equals(Void.TYPE)) {
                MethodType lambdaFunctionSignature = targetMethod.type().changeParameterType(0, Object.class);
                switch (method.getParameterCount()) {
                    case 0: return (Consumer1) getHandle(lookup, targetMethod, Consumer1.class, lambdaFunctionSignature).invokeExact();
                    case 1: return (Consumer2) getHandle(lookup, targetMethod, Consumer2.class, lambdaFunctionSignature).invokeExact();
                    default: throw new RuntimeException("TODO");
                }
            } else {
                MethodType lambdaFunctionSignature = targetMethod.type().generic();
                switch (method.getParameterCount()) {
                    case 0: return (Func1) getHandle(lookup, targetMethod, Func1.class, lambdaFunctionSignature).invokeExact();
                    case 1: return (Func2) getHandle(lookup, targetMethod, Func2.class, lambdaFunctionSignature).invokeExact();
                    case 2: return (Func3) getHandle(lookup, targetMethod, Func3.class, lambdaFunctionSignature).invokeExact();
                    case 3: return (Func4) getHandle(lookup, targetMethod, Func4.class, lambdaFunctionSignature).invokeExact();
                    case 4: return (Func5) getHandle(lookup, targetMethod, Func5.class, lambdaFunctionSignature).invokeExact();
                    default: throw new RuntimeException("TODO");
                }
            }

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private MethodHandle getHandle(
            MethodHandles.Lookup lookup,
            MethodHandle targetMethod,
            Class<?> lambdaFunctionType,
            MethodType lambdaFunctionSignature) throws LambdaConversionException {

        CallSite site = LambdaMetafactory.metafactory(
                lookup,
                "apply",
                MethodType.methodType(lambdaFunctionType),
                lambdaFunctionSignature,
                targetMethod,
                targetMethod.type());

        return site.getTarget();
    }

}
