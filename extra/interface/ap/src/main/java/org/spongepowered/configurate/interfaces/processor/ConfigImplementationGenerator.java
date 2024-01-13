package org.spongepowered.configurate.interfaces.processor;

import static org.spongepowered.configurate.interfaces.processor.Utils.hasAnnotation;

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;

import java.util.List;

class ConfigImplementationGenerator {

    private final ConfigImplementationGeneratorProcessor processor;
    private final TypeElement source;

    ConfigImplementationGenerator(
        final ConfigImplementationGeneratorProcessor processor,
        final TypeElement configInterfaceType
    ) {
        this.processor = processor;
        this.source = configInterfaceType;
    }

    public TypeSpec.Builder generate() {
        final ClassName className = ClassName.get(this.source);

        final TypeSpec.Builder spec = TypeSpec
            .classBuilder(className.simpleName() + "Impl")
            .addSuperinterface(className)
            .addModifiers(Modifier.FINAL)
            .addAnnotation(ConfigSerializable.class)
            .addJavadoc("Automatically generated implementation of the config");

        final TypeSpecBuilderTracker tracker = new TypeSpecBuilderTracker();
        gatherElementSpec(tracker, this.source);
        tracker.writeTo(spec);

        final String qualifiedName = className.reflectionName();
        final String qualifiedImplName = qualifiedName.replace("$", "Impl$") + "Impl";
        this.processor.generatedClasses().put(qualifiedName, qualifiedImplName);

        return spec;
    }

    private void gatherElementSpec(
        final TypeSpecBuilderTracker spec,
        final TypeElement type
    ) {
        // first handle own elements

        for (final Element enclosedElement : type.getEnclosedElements()) {
            final ElementKind kind = enclosedElement.getKind();

            if (kind == ElementKind.INTERFACE && hasAnnotation(enclosedElement, ConfigSerializable.class)) {
                spec.add(
                    enclosedElement.getSimpleName().toString(),
                    new ConfigImplementationGenerator(this.processor, (TypeElement) enclosedElement)
                        .generate()
                        .addModifiers(Modifier.STATIC)
                );
                continue;
            }

            if (kind != ElementKind.METHOD) {
                continue;
            }

            final ExecutableElement element = (ExecutableElement) enclosedElement;

            if (hasAnnotation(element, PostProcess.class)) {
                // A postprocess annotated method is not a config node
                continue;
            }

            final boolean excluded = hasAnnotation(element, Exclude.class);
            if (excluded) {
                if (!element.isDefault()) {
                    this.processor.error(
                            "Cannot make config due to method %s, which is an excluded method that has no implementation!",
                            element
                    );
                }
                continue;
            }

            // all methods are either setters or getters past this point

            final List<? extends VariableElement> parameters = element.getParameters();
            if (parameters.size() > 1) {
                this.processor.error("Setters cannot have more than one parameter! Method: " + element);
                continue;
            }

            final String simpleName = element.getSimpleName().toString();
            TypeMirror nodeType = element.getReturnType();

            if (parameters.size() == 1) {
                // setter
                final VariableElement parameter = parameters.get(0);

                final MethodSpec.Builder method = MethodSpec.overriding(element)
                    .addStatement(
                        "this.$N = $N",
                        element.getSimpleName(),
                        parameter.getSimpleName()
                    );

                // if it's not void
                if (!MoreTypes.isTypeOf(Void.TYPE, nodeType)) {
                    // the return type can be a parent type of parameter, but it has to be assignable
                    if (!this.processor.typeUtils.isAssignable(parameter.asType(), nodeType)) {
                        this.processor.error(
                            "Cannot create a setter with return type %s for argument type %s. Method: %s",
                            nodeType,
                            parameter.asType(),
                            element
                        );
                        continue;
                    }
                    method.addStatement("return this.$N", element.getSimpleName());
                }

                spec.add(simpleName + "#" + parameter.getSimpleName().toString(), method);
                nodeType = parameter.asType();
            } else {
                // getter
                spec.add(
                    simpleName,
                    MethodSpec.overriding(element)
                        .addStatement("return $N", element.getSimpleName())
                );
            }

            final FieldSpec.Builder fieldSpec = FieldSpec.builder(TypeName.get(nodeType), simpleName, Modifier.PRIVATE);

            //todo add tests for hidden in both ap and interfaces and defaults in interfaces
            AnnotationProcessorHandler.handle(element, nodeType, fieldSpec);

            spec.add(simpleName, fieldSpec);
        }

        // then handle parent elements
        for (final TypeMirror parent : type.getInterfaces()) {
            gatherElementSpec(spec, (TypeElement) this.processor.typeUtils.asElement(parent));
        }
    }

}
