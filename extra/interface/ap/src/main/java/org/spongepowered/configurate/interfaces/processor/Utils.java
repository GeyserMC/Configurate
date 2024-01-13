package org.spongepowered.configurate.interfaces.processor;

import com.google.auto.common.MoreTypes;
import java.lang.annotation.Annotation;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

final class Utils {

    private Utils() {}

    static boolean hasAnnotation(final AnnotatedConstruct element, final Class<? extends Annotation> annotation) {
        return annotation(element, annotation) != null;
    }

    /**
     * The same as {@link AnnotatedConstruct#getAnnotation(Class)} except that
     * you don't have to suppress the ConstantValue warning everywhere.
     */
    @SuppressWarnings("DataFlowIssue")
    static <T extends Annotation> @Nullable T annotation(final AnnotatedConstruct construct, final Class<T> annotation) {
        return construct.getAnnotation(annotation);
    }

    static boolean isNestedConfig(final TypeElement type) {
        if (!type.getNestingKind().isNested()) {
            return false;
        }

        Element current = type;
        while (current.getKind() == ElementKind.INTERFACE && hasAnnotation(current, ConfigSerializable.class)) {
            current = current.getEnclosingElement();
        }
        return current.getKind() == ElementKind.PACKAGE;
    }

    static boolean isDecimal(final TypeMirror typeMirror) {
        return MoreTypes.isTypeOf(Float.TYPE, typeMirror) || MoreTypes.isTypeOf(Double.TYPE, typeMirror);
    }

    static boolean isNumeric(final TypeMirror typeMirror) {
        return MoreTypes.isTypeOf(Byte.TYPE, typeMirror) || MoreTypes.isTypeOf(Character.TYPE, typeMirror)
            || MoreTypes.isTypeOf(Short.TYPE, typeMirror) || MoreTypes.isTypeOf(Integer.TYPE, typeMirror)
            || MoreTypes.isTypeOf(Long.TYPE, typeMirror);
    }

}
