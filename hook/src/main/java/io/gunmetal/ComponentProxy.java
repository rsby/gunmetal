package io.gunmetal;

import io.gunmetal.function.FuncAny;
import io.gunmetal.function.FuncFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author rees.byars
 */
class ComponentProxy implements InvocationHandler {

    private final List<ComponentVisitor> componentVisitors;
    private final FuncFactory funcFactory;
    private final ConcurrentMap<Method, FuncAny> funcAnyCache = new ConcurrentHashMap<>();

    ComponentProxy(
            FuncFactory funcFactory,
            List<ComponentVisitor> componentVisitors) {
        this.funcFactory = funcFactory;
        this.componentVisitors = componentVisitors.isEmpty() ?
                Collections.singletonList(new ScopeDecorator()) :
                componentVisitors;
    }

    @Override public Object invoke(
            Object proxy,
            Method method,
            Object[] args) throws Throwable {

        if (!method.isDefault()) {
            throw new RuntimeException("All component methods must have a 'default' implementation");
        }

        FuncAny funcAny = funcAnyCache.computeIfAbsent(method, (m) -> {
            try {
                FuncAny f = funcFactory.createFor(m);
                for (ComponentVisitor componentVisitor : componentVisitors) {
                    f = componentVisitor.visitFunction(f, m);
                }
                return f;
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });

        Object o = null;
        for (ComponentVisitor componentVisitor : componentVisitors) {
            o = componentVisitor.visitInvocation(
                    funcAny,
                    method,
                    proxy,
                    args);
        }
        return o;

    }

}
