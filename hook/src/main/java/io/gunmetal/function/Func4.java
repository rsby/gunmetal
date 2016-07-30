package io.gunmetal.function;

/**
 * @author rees.byars
 */
@FunctionalInterface
public interface Func4 extends FuncAny {

    Object apply(Object target, Object p1, Object p2, Object p3);

    default Object apply(Object target, Object[] args) {
        return apply(target, args[0], args[1], args[2]);
    }

}
