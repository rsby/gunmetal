package io.gunmetal.function;

import java.lang.reflect.Method;

/**
 * @author rees.byars
 */
public interface FuncFactory {

    FuncAny createFor(Method method) throws Throwable;

}
