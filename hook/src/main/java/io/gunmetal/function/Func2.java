package io.gunmetal.function;

/**
 * @author rees.byars
 */
@FunctionalInterface
public interface Func2 extends FuncAny {

    Object apply(Object target, Object p);

    default Object apply(Object target, Object[] args) {
        return apply(target, args[0]);
    }

}
