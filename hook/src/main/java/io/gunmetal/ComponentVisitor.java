package io.gunmetal;

import io.gunmetal.function.FuncAny;

import java.lang.reflect.Method;

/**
 * @author rees.byars
 */
public interface ComponentVisitor {

    void visitComponent(Class<?> componentInterface);

    FuncAny visitFunction(FuncAny funcAny, Method method);

    Object visitInvocation(FuncAny funcAny, Method method, Object target, Object[] args);

}
