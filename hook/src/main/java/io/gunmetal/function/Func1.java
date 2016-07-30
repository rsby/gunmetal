package io.gunmetal.function;

/**
 * @author rees.byars
 */
@FunctionalInterface
public interface Func1 extends FuncAny {

    Object apply(Object target);

    default Object apply(Object target, Object[] args) {
        return apply(target);
    }

}
