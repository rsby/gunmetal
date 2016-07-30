package io.gunmetal;

import io.gunmetal.function.FuncAny;

import java.lang.reflect.Method;

/**
 * @author rees.byars
 */
public class ScopeDecorator implements ComponentVisitor {

    @Override
    public void visitComponent(Class<?> componentInterface) {
        // do nothing
    }

    @Override
    public FuncAny visitFunction(FuncAny funcAny, Method method) {
        if (method.isAnnotationPresent(Singleton.class)) {
            return new FuncAny() {
                volatile Object singleton;
                @Override
                public Object apply(Object target, Object[] args) {
                    if (singleton == null) {
                        synchronized (this) {
                            if (singleton == null) {
                                singleton = funcAny.apply(target, args);
                            }
                        }
                    }
                    return singleton;
                }
            };
        }
        return funcAny;
    }

    @Override
    public Object visitInvocation(FuncAny funcAny, Method method, Object target, Object[] args) {
        // do nothing
        return funcAny.apply(target, args);
    }

}
