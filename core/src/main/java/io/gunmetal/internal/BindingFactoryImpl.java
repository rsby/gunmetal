/*
 * Copyright (c) 2013.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gunmetal.internal;

import io.gunmetal.BlackList;
import io.gunmetal.Module;
import io.gunmetal.WhiteList;
import io.gunmetal.spi.Dependency;
import io.gunmetal.spi.DependencyRequest;
import io.gunmetal.spi.ModuleMetadata;
import io.gunmetal.spi.Qualifier;
import io.gunmetal.spi.QualifierResolver;
import io.gunmetal.spi.RequestVisitor;
import io.gunmetal.spi.ResourceMetadata;
import io.gunmetal.spi.ResourceMetadataResolver;
import io.gunmetal.spi.Scopes;
import io.gunmetal.spi.TypeKey;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author rees.byars
 */
class BindingFactoryImpl implements BindingFactory {

    private final ResourceFactory resourceFactory;
    private final QualifierResolver qualifierResolver;
    private final ResourceMetadataResolver resourceMetadataResolver;
    private final boolean requireExplicitModuleDependencies;

    BindingFactoryImpl(ResourceFactory resourceFactory,
                       QualifierResolver qualifierResolver,
                       ResourceMetadataResolver resourceMetadataResolver,
                       boolean requireExplicitModuleDependencies) {
        this.resourceFactory = resourceFactory;
        this.qualifierResolver = qualifierResolver;
        this.resourceMetadataResolver = resourceMetadataResolver;
        this.requireExplicitModuleDependencies = requireExplicitModuleDependencies;
    }

    @Override public List<Binding<?>> createBindingsForModule(final Class<?> module,
                                                              GraphContext context,
                                                              Set<Class<?>> loadedModules) {
        if (loadedModules.contains(module)) {
            return Collections.emptyList();
        }
        loadedModules.add(module);

        final Module moduleAnnotation = module.getAnnotation(Module.class);
        if (moduleAnnotation == null) {
            context.errors().add("The module class [" + module.getName()
                    + "] must be annotated with @Module()");
            return Collections.emptyList();
        }
        final RequestVisitor moduleRequestVisitor = moduleRequestVisitor(module, moduleAnnotation, context);
        final ModuleMetadata moduleMetadata = moduleMetadata(module, moduleAnnotation, context);
        final List<Binding<?>> resourceBindings = new ArrayList<>();
        addResourceBindings(
                moduleAnnotation,
                module,
                resourceBindings,
                moduleMetadata,
                moduleRequestVisitor,
                context,
                loadedModules);
        return resourceBindings;

    }

    @Override public <T> Binding<T> createJitBindingForRequest(
            DependencyRequest<T> dependencyRequest, GraphContext context) {
        Dependency<T> dependency = dependencyRequest.dependency();
        TypeKey<T> typeKey = dependency.typeKey();
        if (!isInstantiable(typeKey.raw())) {
            return null;
        }
        Class<? super T> cls = typeKey.raw();
        ModuleMetadata moduleMetadata = dependencyRequest.sourceModule(); // essentially, same as library
        Resource<T> resource = resourceFactory.withClassProvider(
                resourceMetadataResolver.resolveMetadata(cls, moduleMetadata, context.errors()), context);
        if (!resource.metadata().qualifier().equals(dependency.qualifier())) {
            return null;
        }
        return new BindingImpl<>(
                resource,
                Collections.<Dependency<? super T>>singletonList(dependency),
                moduleMetadata.moduleAnnotation() == Module.NONE ?
                        RequestVisitor.NONE :
                        moduleRequestVisitor(moduleMetadata.moduleClass(), moduleMetadata.moduleAnnotation(), context),
                decorateForModule(moduleMetadata, AccessFilter.create(cls)));
    }

    private RequestVisitor moduleRequestVisitor(final Class<?> module,
                                                final Module moduleAnnotation,
                                                GraphContext context) {
        final RequestVisitor blackListVisitor = blackListVisitor(module, moduleAnnotation, context);
        final RequestVisitor whiteListVisitor = whiteListVisitor(module, moduleAnnotation, context);
        final RequestVisitor dependsOnVisitor = dependsOnVisitor(module);
        final AccessFilter<Class<?>> classAccessFilter = AccessFilter.create(moduleAnnotation.access(), module);
        final RequestVisitor moduleClassVisitor = (dependencyRequest, errors) -> {
            if (!classAccessFilter.isAccessibleTo(dependencyRequest.sourceModule().moduleClass())) {
                errors.add(
                        "The module [" + dependencyRequest.sourceModule().moduleClass().getName()
                                + "] does not have access to [" + classAccessFilter.filteredElement() + "]"
                );
            }
        };
        return (dependencyRequest, errors) -> {
            moduleClassVisitor.visit(dependencyRequest, errors);
            dependsOnVisitor.visit(dependencyRequest, errors);
            blackListVisitor.visit(dependencyRequest, errors);
            whiteListVisitor.visit(dependencyRequest, errors);
        };
    }

    private ModuleMetadata moduleMetadata(final Class<?> module, final Module moduleAnnotation, GraphContext context) {
        final Qualifier qualifier = qualifierResolver.resolve(module, context.errors());
        return new ModuleMetadata(module, qualifier, moduleAnnotation);
    }

    private RequestVisitor blackListVisitor(final Class<?> module, Module moduleAnnotation, GraphContext context) {

        Class<? extends BlackList> blackListConfigClass =
                moduleAnnotation.notAccessibleFrom();

        if (blackListConfigClass == BlackList.class) {
            return RequestVisitor.NONE;
        }

        final Class<?>[] blackListClasses;

        BlackList.Modules blackListModules =
                blackListConfigClass.getAnnotation(BlackList.Modules.class);

        if (blackListModules != null) {
            blackListClasses = blackListModules.value();
        } else {
            blackListClasses = new Class<?>[]{};
        }

        final Qualifier blackListQualifier = qualifierResolver.resolve(blackListConfigClass, context.errors());

        return (dependencyRequest, errors) -> {

            Class<?> requestingSourceModuleClass = dependencyRequest.sourceModule().moduleClass();
            for (Class<?> blackListClass : blackListClasses) {
                if (blackListClass == requestingSourceModuleClass) {
                    errors.add("The module [" + requestingSourceModuleClass.getName()
                            + "] does not have access to the module [" + module.getName() + "].");
                }
            }

            boolean qualifierMatch =
                    blackListQualifier.qualifiers().length > 0
                    && dependencyRequest.sourceQualifier().qualifiers().length > 0
                    && dependencyRequest.sourceQualifier().intersects(blackListQualifier);

            if (qualifierMatch) {
                errors.add("The module [" + requestingSourceModuleClass.getName()
                        + "] does not have access to the module [" + module.getName() + "].");
            }
        };
    }

    private RequestVisitor whiteListVisitor(final Class<?> module, Module moduleAnnotation, GraphContext context) {

        Class<? extends WhiteList> whiteListConfigClass =
                moduleAnnotation.onlyAccessibleFrom();

        if (whiteListConfigClass == WhiteList.class) {
            return RequestVisitor.NONE;
        }

        final Class<?>[] whiteListClasses;

        WhiteList.Modules whiteListModules =
                whiteListConfigClass.getAnnotation(WhiteList.Modules.class);

        if (whiteListModules != null) {
            whiteListClasses = whiteListModules.value();
        } else {
            whiteListClasses = new Class<?>[]{};
        }

        final Qualifier whiteListQualifier = qualifierResolver.resolve(whiteListConfigClass, context.errors());

        return (dependencyRequest, errors) -> {

            Class<?> requestingSourceModuleClass = dependencyRequest.sourceModule().moduleClass();
            for (Class<?> whiteListClass : whiteListClasses) {
                if (whiteListClass == requestingSourceModuleClass) {
                    return;
                }
            }

            boolean qualifierMatch = dependencyRequest.sourceQualifier().intersects(whiteListQualifier);
            if (!qualifierMatch) {
                errors.add("The module [" + requestingSourceModuleClass.getName()
                        + "] does not have access to the module [" + module.getName() + "].");
            }
        };
    }

    private RequestVisitor dependsOnVisitor(final Class<?> module) {

        return (dependencyRequest, errors) -> {

            ModuleMetadata requestSourceModule = dependencyRequest.sourceModule();

            if (module == requestSourceModule.moduleClass()) {
                return;
            }

            if (requestSourceModule.referencedModules().length == 0 && !requireExplicitModuleDependencies) {
                return;
            }

            for (Class<?> dependency : requestSourceModule.referencedModules()) {
                if (module == dependency) {
                    return;
                }
            }

            errors.add("The module [" + requestSourceModule.moduleClass().getName()
                    + "] does not have access to the module [" + module.getName() + "].");

        };

    }

    private void addResourceBindings(Module moduleAnnotation,
                                    Class<?> module,
                                    List<Binding<?>> resourceBindings,
                                    ModuleMetadata moduleMetadata,
                                    RequestVisitor moduleRequestVisitor,
                                    GraphContext context,
                                    Set<Class<?>> loadedModules) {
        if (!module.isInterface() && module.getSuperclass() != Object.class) {
            context.errors().add("The module " + module.getName() + " extends a class other than Object");
        }
        if (moduleAnnotation.stateful()) {
            if (!moduleAnnotation.provided()) {
                resourceBindings.add(managedModuleResourceBinding(module, moduleMetadata, context));
            } else {
                resourceBindings.add(providedModuleResourceBinding(module, moduleMetadata, context));
            }
            Arrays.stream(module.getDeclaredMethods()).filter(m -> !m.isSynthetic()).forEach(m -> {
                resourceBindings.add(statefulResourceBinding(m, module, moduleRequestVisitor, moduleMetadata, context));
            });
        } else {
            Arrays.stream(module.getDeclaredMethods()).filter(m -> !m.isSynthetic()).forEach(m -> {
                ResourceMetadata<Method> resourceMetadata =
                        resourceMetadataResolver.resolveMetadata(m, moduleMetadata, context.errors());
                if (resourceMetadata.isProvider()) {
                    resourceBindings.add(
                            resourceBinding(resourceMetadata, module, moduleRequestVisitor, moduleMetadata, context));
                }
            });
        }
        for (Class<?> library : moduleAnnotation.subsumes()) {
            Module libModule = library.getAnnotation(Module.class);
            // TODO allow provided? require prototype?
            Qualifier libQualifier = qualifierResolver.resolve(library, context.errors());
            if (libModule == null) {
                context.errors().add("A class without @Module cannot be subsumed");
            } else if (!libModule.lib()) {
                context.errors().add("@Module.lib must be true to be subsumed");
            } else if (libQualifier != Qualifier.NONE) {
                context.errors().add("Library " + library.getName() + " should not have a qualifier -> " + libQualifier);
            }
            addResourceBindings(
                    libModule,
                    library,
                    resourceBindings,
                    moduleMetadata,
                    moduleRequestVisitor,
                    context,
                    loadedModules);
        }
        for (Class<?> m : moduleAnnotation.dependsOn()) {
            resourceBindings.addAll(createBindingsForModule(m, context, loadedModules));
        }
    }

    private AccessFilter<Class<?>> decorateForModule(ModuleMetadata moduleMetadata,
                                                     AccessFilter<Class<?>> accessFilter) {
        return new AccessFilter<Class<?>>() {
            @Override public AnnotatedElement filteredElement() {
                return accessFilter.filteredElement();
            }
            @Override public boolean isAccessibleTo(Class<?> target) {
                // supports library access
                return target == moduleMetadata.moduleClass() || accessFilter.isAccessibleTo(target);
            }
        };
    }

    private <T> Binding<T> resourceBinding(
            ResourceMetadata<Method> resourceMetadata,
            Class<?> module,
            RequestVisitor moduleRequestVisitor,
            ModuleMetadata moduleMetadata,
            GraphContext context) {

        Method method = resourceMetadata.provider();

        int modifiers = method.getModifiers();

        if (module.isInterface() && !Modifier.isStatic(modifiers)) {
            throw new IllegalArgumentException("A module's provider methods must be static.  The method ["
                    + method.getName() + "] in module [" + module.getName() + "] is not static.");
        }

        if (method.getReturnType() == void.class) {
            throw new IllegalArgumentException("A module's provider methods cannot have a void return type.  The method ["
                    + method.getName() + "] in module [" + module.getName() + "] is returns void.");
        }

        // if (resourceMetadata.isModule()) {
            // TODO
        // }

        // TODO targeted return type check
        final List<Dependency<? super T>> dependencies = Collections.<Dependency<? super T>>singletonList(
                Dependency.from(resourceMetadata.qualifier(), method.getGenericReturnType()));

        return new BindingImpl<>(
                resourceFactory.<T>withMethodProvider(resourceMetadata, context),
                dependencies,
                moduleRequestVisitor,
                decorateForModule(moduleMetadata, AccessFilter.create(method)));
    }

    private <T> Binding<T> statefulResourceBinding(
            final Method method,
            Class<?> module,
            final RequestVisitor moduleRequestVisitor,
            final ModuleMetadata moduleMetadata,
            GraphContext context) {

        if (method.getReturnType() == void.class) {
            throw new IllegalArgumentException("A module's provider methods cannot have a void return type.  The method ["
                    + method.getName() + "] in module [" + module.getName() + "] is returns void.");
        }

        ResourceMetadata<Method> resourceMetadata =
                resourceMetadataResolver.resolveMetadata(method, moduleMetadata, context.errors());

        Dependency<T> provisionDependency =
                Dependency.from(resourceMetadata.qualifier(), method.getGenericReturnType());

        Dependency<?> moduleDependency =
                Dependency.from(moduleMetadata.qualifier(), module);


        // TODO targeted return type check
        final List<Dependency<? super T>> dependencies =
                Collections.<Dependency<? super T>>singletonList(provisionDependency);

        if (Modifier.isStatic(resourceMetadata.provider().getModifiers())) {
            return new BindingImpl<>(
                    resourceFactory.<T>withMethodProvider(resourceMetadata, context),
                    dependencies,
                    moduleRequestVisitor,
                    decorateForModule(moduleMetadata, AccessFilter.create(method)));
        }

        return new BindingImpl<>(
                resourceFactory.<T>withStatefulMethodProvider(resourceMetadata, moduleDependency, context),
                dependencies,
                moduleRequestVisitor,
                decorateForModule(moduleMetadata, AccessFilter.create(method)));
    }

    private <T> Binding<T> managedModuleResourceBinding(Class<T> module,
                                                                        ModuleMetadata moduleMetadata,
                                                                        GraphContext context) {
        Dependency<T> dependency = Dependency.from(moduleMetadata.qualifier(), module);
        Resource<T> resource = resourceFactory.withClassProvider(
                resourceMetadataResolver.resolveMetadata(module, moduleMetadata, context.errors()), context);
        return new BindingImpl<>(
                resource,
                Collections.<Dependency<? super T>>singletonList(dependency),
                (dependencyRequest, errors) -> {
                    if (!dependencyRequest.sourceModule().equals(moduleMetadata)) {
                        errors.add("Module can only be requested by its providers"); // TODO
                    }
                },
                decorateForModule(moduleMetadata, AccessFilter.create(module)));
    }

    private <T> Binding<T> providedModuleResourceBinding(Class<T> module,
                                                                        ModuleMetadata moduleMetadata,
                                                                        GraphContext context) {
        Dependency<T> dependency = Dependency.from(moduleMetadata.qualifier(), module);
        ResourceMetadata<Class<?>> resourceMetadata =
                resourceMetadataResolver.resolveMetadata(module, moduleMetadata, context.errors());
        if (resourceMetadata.scope() != Scopes.SINGLETON) {
            // TODO context.errors().add(resourceMetadata, "Provided modules must have a scope of singleton");
        }
        Resource<T> resource = resourceFactory.withProvidedModule(
                resourceMetadata, context);
        return new BindingImpl<>(
                resource,
                Collections.<Dependency<? super T>>singletonList(dependency),
                (dependencyRequest, dependencyResponse) -> {
                    if (!dependencyRequest.sourceModule().equals(moduleMetadata)) {
                        dependencyResponse.add("Module can only be requested by its providers"); // TODO
                    }
                },
                decorateForModule(moduleMetadata, AccessFilter.create(module)));
    }

    private boolean isInstantiable(Class<?> cls) {
        if (cls.isInterface()
                || cls.isArray()
                || cls.isEnum()
                || cls.isPrimitive()
                || cls.isSynthetic()
                || cls.isAnnotation()) {
            return false;
        }
        int modifiers = cls.getModifiers();
        return !Modifier.isAbstract(modifiers);
    }

}