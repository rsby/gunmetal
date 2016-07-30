package io.gunmetal;

import io.gunmetal.function.FuncFactory;
import io.gunmetal.function.LambdaFuncFactory;
import io.gunmetal.function.MethodHandleFuncFactory;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author rees.byars
 */
public class Components {

    public static <T> T newInstance(
            Class<T> componentInterface, ComponentVisitor... componentVisitors) {
        if (!componentInterface.isInterface()) {
            throw new RuntimeException("The component class must be an interface");
        }
        Object component = Proxy.newProxyInstance(
                componentInterface.getClassLoader(),
                new Class[]{ componentInterface },
                new ComponentProxy(
                        new MethodHandleFuncFactory(),
                        Arrays.asList(componentVisitors)));
        return componentInterface.cast(component);
    }

    public static <T> Supplier<T> newSupplier(
            Class<T> componentInterface, ComponentVisitor... componentVisitors) {
        if (!componentInterface.isInterface()) {
            throw new RuntimeException("The component class must be an interface");
        }
        FuncFactory funcFactory = new LambdaFuncFactory();
        return () -> {
            // TODO reuse proxy constructor
            Object component = Proxy.newProxyInstance(
                    componentInterface.getClassLoader(),
                    new Class[]{ componentInterface },
                    new ComponentProxy(
                            funcFactory,
                            Arrays.asList(componentVisitors)));
            return componentInterface.cast(component);
        };
    }

}
