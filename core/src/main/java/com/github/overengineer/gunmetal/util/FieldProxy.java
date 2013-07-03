package com.github.overengineer.gunmetal.util;

import com.github.overengineer.gunmetal.inject.InjectionException;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author rees.byars
 */
public interface FieldProxy extends Serializable, TypeRef {

    void set(Object target, Object value);
    Object get(Object target);
    Type getType();
    Class<?> getDeclaringClass();
    boolean isDecorated();

    class Factory {

        private static class Proxy implements FieldProxy {

            final FieldRef fieldRef;

            Proxy(FieldRef fieldRef) {
                this.fieldRef = fieldRef;
            }

            @Override
            public void set(Object target, Object value) {
                try {
                    fieldRef.getField().set(target, value);
                }  catch (Exception e) {
                    throw new InjectionException("Could not inject field [" + fieldRef.getField().getName() + "] on component of type [" + fieldRef.getField().getDeclaringClass().getName() + "].", e);
                }
            }

            @Override
            public Object get(Object target) {
                try {
                    return fieldRef.getField().get(target);
                }  catch (Exception e) {
                    throw new InjectionException("Could not retrieve field [" + fieldRef.getField().getName() + "] from component of type [" + fieldRef.getField().getDeclaringClass().getName() + "].", e);
                }
            }

            @Override
            public Type getType() {
                return fieldRef.getField().getGenericType();
            }

            @Override
            public Class<?> getDeclaringClass() {
                return fieldRef.getField().getDeclaringClass();
            }

            @Override
            public boolean isDecorated() {
                return fieldRef.getField().getType().isAssignableFrom(fieldRef.getField().getDeclaringClass());
            }
        }

        public static FieldProxy create(Field field) {
            return new Proxy(new FieldRefImpl(field));
        }

    }

}
