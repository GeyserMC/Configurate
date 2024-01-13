package org.spongepowered.configurate.interfaces.processor;

import static org.spongepowered.configurate.interfaces.processor.Utils.annotation;

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.AnnotationSpec;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultBoolean;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultDecimal;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultNumeric;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultString;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class AnnotationDefaults implements AnnotationProcessor {

    static final AnnotationDefaults INSTANCE = new AnnotationDefaults();

    private AnnotationDefaults() {}

    @Override
    public Set<Class<? extends Annotation>> processes() {
        return new HashSet<>(Arrays.asList(DefaultBoolean.class, DefaultDecimal.class, DefaultNumeric.class, DefaultString.class));
    }

    @Override
    public void process(
            final ExecutableElement element,
            final TypeMirror nodeType,
            final FieldSpecBuilderTracker fieldSpec
    ) throws IllegalStateException {
        final @Nullable DefaultBoolean defaultBoolean = annotation(element, DefaultBoolean.class);
        final @Nullable DefaultDecimal defaultDecimal = annotation(element, DefaultDecimal.class);
        final @Nullable DefaultNumeric defaultNumeric = annotation(element, DefaultNumeric.class);
        final @Nullable DefaultString defaultString = annotation(element, DefaultString.class);
        final boolean hasDefault = defaultBoolean != null || defaultDecimal != null || defaultNumeric != null || defaultString != null;

        @Nullable Class<? extends Annotation> annnotationType = null;
        @Nullable Object value = null;
        if (hasDefault) {
            if (MoreTypes.isTypeOf(Boolean.TYPE, nodeType)) {
                if (defaultBoolean == null) {
                    throw new IllegalStateException("A default value of the incorrect type was provided for " + element);
                }
                annnotationType = DefaultBoolean.class;
                value = defaultBoolean.value();

            } else if (Utils.isDecimal(nodeType)) {
                if (defaultDecimal == null) {
                    throw new IllegalStateException("A default value of the incorrect type was provided for " + element);
                }
                annnotationType = DefaultDecimal.class;
                value = defaultDecimal.value();

            } else if (Utils.isNumeric(nodeType)) {
                if (defaultNumeric == null) {
                    throw new IllegalStateException("A default value of the incorrect type was provided for " + element);
                }
                annnotationType = DefaultNumeric.class;
                value = defaultNumeric.value();

            } else if (MoreTypes.isTypeOf(String.class, nodeType)) {
                if (defaultString == null) {
                    throw new IllegalStateException("A default value of the incorrect type was provided for " + element);
                }
                annnotationType = DefaultString.class;
                value = defaultString.value();
            }
        }

        if (annnotationType == null) {
            return;
        }

        final boolean isString = value instanceof String;

        // special cases are floats and longs, because the default for decimals
        // is double and for numerics it's int.
        if (MoreTypes.isTypeOf(Float.TYPE, nodeType)) {
            value = value + "F";
        } else if (MoreTypes.isTypeOf(Long.TYPE, nodeType)) {
            value = value + "L";
        }

        fieldSpec.addAnnotation(
                AnnotationSpec.builder(annnotationType)
                        .addMember("value", isString ? "$S" : "$L", value)
        );
        fieldSpec.initializer(isString ? "$S" : "$L", value);
    }

}
