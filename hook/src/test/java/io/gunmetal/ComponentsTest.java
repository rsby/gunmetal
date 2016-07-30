package io.gunmetal;

import io.gunmetal.function.FuncAny;
import org.junit.Test;

import java.lang.reflect.Method;

public class ComponentsTest {

    @Test
    public void testCreate() throws Exception {

        TestComponent t = Components.newInstance(TestComponent.class,
                new ComponentVisitor() {
                    @Override public void visitComponent(Class<?> componentInterface) {

                    }

                    @Override public FuncAny visitFunction(FuncAny funcAny, Method method) {
                        return funcAny;
                    }

                    @Override public Object visitInvocation(FuncAny funcAny, Method method, Object target, Object[] args) {
                        System.out.println("invoking " + method.getName());
                        return funcAny.apply(target, args);
                    }
                });

        t.grr();
        System.out.println(t.object("hi"));


        //System.out.println(Components.newInstance(TestComponent.class).object("ho"));
    }

}