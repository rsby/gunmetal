package io.gunmetal.compiler;

import io.gunmetal.Provider;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor8;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rees.byars
 */
class BindingFactory implements Factory<Binding> {

    private final Factory<MemberMetadata> memberMetadataFactory;
    private final Factory<ProviderLocation> providerLocationFactory;
    private final Provider<Builder<Qualifier>> qualifierBuilderProvider;

    BindingFactory(
            Factory<MemberMetadata> memberMetadataFactory,
            Factory<ProviderLocation> providerLocationFactory,
            Provider<Builder<Qualifier>> qualifierBuilderProvider) {
        this.memberMetadataFactory = memberMetadataFactory;
        this.providerLocationFactory = providerLocationFactory;
        this.qualifierBuilderProvider = qualifierBuilderProvider;
    }

    @Override public Binding create(Element providerElement) {

        ExecutableReport report = new ExecutableReport(providerElement);

        ProviderLocation location = providerLocationFactory.create(providerElement);
        MemberMetadata providerMetadata = memberMetadataFactory.create(providerElement);
        Qualifier mergedQualifier = location.metadata().qualifier().merge(providerMetadata.qualifier());
        // TODO should this be handled in the memberMetadataFactory?
        providerMetadata = new MemberMetadata(mergedQualifier, providerMetadata.scope(), providerElement);

        Dependency fulfilledDependency = new Dependency(report.producedType, providerMetadata.qualifier());

        List<Dependency> requiredDependencies = new ArrayList<>();
        for (VariableElement parameterElement : report.parameterElements) {
            Builder<Qualifier> qualifierBuilder = qualifierBuilderProvider.get();
            new AnnotatedElement(parameterElement).accept(qualifierBuilder);
            requiredDependencies.add(new Dependency(parameterElement.asType(), qualifierBuilder.build()));
        }

        return new Binding(
                providerMetadata,
                ProviderKind.fromElement(providerElement),
                location,
                fulfilledDependency, requiredDependencies);
    }

    private static class ExecutableReport {

        TypeMirror producedType;

        List<? extends VariableElement> parameterElements;

        ExecutableReport(Element element) {

            element.accept(new ElementKindVisitor8<ExecutableElement, Void>() {

                @Override public ExecutableElement visitExecutableAsConstructor(
                        ExecutableElement constructorElement, Void v) {
                    producedType = constructorElement.getEnclosingElement().asType();
                    parameterElements = constructorElement.getParameters();
                    return null;
                }

                @Override public ExecutableElement visitExecutableAsMethod(
                        ExecutableElement methodElement, Void p) {
                    producedType = methodElement.getReturnType();
                    parameterElements = methodElement.getParameters();
                    return null;
                }

            }, null);

        }

    }

}