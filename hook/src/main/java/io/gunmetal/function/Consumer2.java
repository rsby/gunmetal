package io.gunmetal.function;

/**
 * @author rees.byars
 */
@FunctionalInterface
public interface Consumer2 extends FuncAny {

    void apply(Object target, Object p1);

    default Object apply(Object target, Object[] args) {
        apply(target, args[0]);
        return null;
    }

}
