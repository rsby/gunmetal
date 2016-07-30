package io.gunmetal.function;

/**
 * @author rees.byars
 */
@FunctionalInterface
public interface Func3 extends FuncAny {

    Object apply(Object target, Object p1, Object p2);

    default Object apply(Object target, Object[] args) {
        return apply(target, args[0], args[1]);
    }

}
