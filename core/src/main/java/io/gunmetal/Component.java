package io.gunmetal;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rees.byars
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    Class<?> type();
    Class<? extends Annotation> scope() default Scope.class; //undefined scope
    AccessLevel access() default AccessLevel.UNDEFINED;
    Class<?>[] targets() default { };
}