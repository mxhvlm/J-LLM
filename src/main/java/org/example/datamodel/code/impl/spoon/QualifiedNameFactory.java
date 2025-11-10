package org.example.datamodel.code.impl.spoon;

import org.example.datamodel.code.EnumQualifiedNameSeparator;
import org.example.datamodel.code.QualifiedName;
import spoon.reflect.CtModelImpl;
import spoon.reflect.declaration.*;

import java.util.ArrayDeque;
import java.util.LinkedList;

class QualifiedNameFactory {

    private static EnumQualifiedNameSeparator getSeparatorForElement(CtElement element) {
        if (element.isParentInitialized()) {
            CtElement parent = element.getParent();
            if (parent != null) {
                if (element instanceof CtType<?> && parent instanceof CtType<?>) {
                    return EnumQualifiedNameSeparator.INNER_TYPE;
                }
            }
        }

        if (element instanceof CtMethod<?>) {
            return EnumQualifiedNameSeparator.METHOD;
        }

        if (element instanceof CtParameter<?>) {
            return EnumQualifiedNameSeparator.PARAMETER;
        }

        return EnumQualifiedNameSeparator.OTHER;
    }

    public static QualifiedName fromCtElement(CtNamedElement element) {
        assert element != null;

        ArrayDeque<QualifiedName.Part> parts = new ArrayDeque<>();

        CtElement current = element;
        while(true) {
            if (current instanceof CtNamedElement namedElement) {
                String name;
                if (current instanceof CtMethod<?> method) {
                    name = method.getSignature();
                } else {
                    name = namedElement.getSimpleName();
                }
                parts.push(new QualifiedName.Part(name, getSeparatorForElement(current)));
            }

            if (current == null || !current.isParentInitialized() || current.getParent() instanceof CtModelImpl.CtRootPackage) {
                break;
            }
            current = current.getParent();
        }

        return new QualifiedName(new LinkedList<>(parts));
    }
}
