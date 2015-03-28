package io.gunmetal.internal;

import io.gunmetal.spi.Errors;
import io.gunmetal.spi.Linkers;
import io.gunmetal.spi.ProvisionStrategyDecorator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author rees.byars
 */
class ComponentContext {

    private final ProvisionStrategyDecorator strategyDecorator;
    private final Linkers linkers;
    private final Errors errors;
    private final Set<Class<?>> loadedModules = new HashSet<>();
    private final Map<Class<?>, Object> statefulSources;

    ComponentContext(ProvisionStrategyDecorator strategyDecorator,
                     Linkers linkers,
                     Errors errors,
                     Map<Class<?>, Object> statefulSources) {
        this.strategyDecorator = strategyDecorator;
        this.linkers = linkers;
        this.errors = errors;
        this.statefulSources = statefulSources;
    }

    ProvisionStrategyDecorator strategyDecorator() {
        return strategyDecorator;
    }

    Linkers linkers() {
        return linkers;
    }

    Errors errors() {
        return errors;
    }

    Set<Class<?>> loadedModules() {
        return loadedModules;
    }

    Object statefulSource(Class<?> sourceClass) {
        return statefulSources.get(sourceClass);
    }

}