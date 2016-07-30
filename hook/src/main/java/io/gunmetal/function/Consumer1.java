package io.gunmetal.function;

/**
 * @author rees.byars
 */
@FunctionalInterface
public interface Consumer1 extends FuncAny {

    void apply(Object target);

    default Object apply(Object target, Object[] args) {
        apply(target);
        return null;
    }

}
