package io.gunmetal.spi;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;

/**
 * @author rees.byars
 */
public interface ResourceMetadataResolver {

    <T extends AnnotatedElement & Member> ResourceMetadata<T> resolveMetadata(
            T annotatedElement,
            ModuleMetadata moduleMetadata,
            Errors errors);

    <T extends Parameter> ResourceMetadata<T> resolveMetadata(
            T annotatedElement,
            ModuleMetadata moduleMetadata,
            Errors errors);

    ResourceMetadata<Class<?>> resolveMetadata(
            Class<?> cls,
            ModuleMetadata moduleMetadata,
            Errors errors);

}
