package io.gunmetal.compiler;

import io.gunmetal.Provider;
import io.gunmetal.Provides;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author rees.byars
 */
@SupportedAnnotationTypes("io.gunmetal.Provides")
public class ProviderProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        // Setup
        Provider<Builder<Qualifier>> qualifierBuilderProvider = QualifierBuilder::new;
        Provider<Builder<Scope>> scopeBuilderProvider = ScopeBuilder::new;
        Factory<MemberMetadata> memberMetadataFactory =
                new MemberMetadataFactory(qualifierBuilderProvider, scopeBuilderProvider);
        Factory<ProviderLocation> providerLocationFactory =
                new ProviderLocationFactory(memberMetadataFactory);
        Factory<Binding> bindingFactory =
                new BindingFactory(memberMetadataFactory, providerLocationFactory, qualifierBuilderProvider);

        // Parse bindings
        Set<? extends Element> providesElements = roundEnv.getElementsAnnotatedWith(Provides.class);
        Map<Dependency, Binding> bindings = new HashMap<>();
        for (Element providerElement : providesElements) {
            Binding binding = bindingFactory.create(providerElement);
            bindings.put(binding.fulfilledDependency(), binding);
            // TODO, go ahead and add bindings for Ref and Provider? RefBindFactory and ProviderBindingFactory
        }

        // Validate graph
        ProviderNames providerNames = new ProviderNames();
        for (Binding binding : bindings.values()) {
            providerNames.getProviderNameFor(binding); // TODO this is randomish
            for (Dependency dependency : binding.requiredDependencies()) {
                Binding dependencyBinding = bindings.get(dependency);
                if (dependencyBinding == null) {
                    throw new RuntimeException(dependency.toString()); // TODO
                }
            }
        }

        // Generate code
        ProviderWriter writer = new ProviderWriter(providerNames, processingEnv.getFiler());
        for (Binding binding : bindings.values()) {
            try {
                writer.writeProviderFor(binding);
            } catch (IOException e) {
                throw new RuntimeException(e); // TODO
            }
        }

        return false;

    }

}
