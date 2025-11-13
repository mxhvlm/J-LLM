package org.example.datamodel.impl.code.spoon;

import org.example.datamodel.api.code.wrapper.*;
import org.example.datamodel.impl.code.wrapper.AbstractWrappedMethod;
import org.example.datamodel.impl.code.wrapper.CodeObjectRegistry;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WrappedCtMethod<T extends CtMethod<?>> extends AbstractWrappedMethod<T> {
    public WrappedCtMethod(T wrappedObject, INamedElement parent) {
        super(wrappedObject, parent, QualifiedNameFactory.fromCtElement(wrappedObject));
    }

    @Override
    protected List<String> resolveModifiers(CodeObjectRegistry registry) {
        return getWrappedObject().getModifiers()
                .stream()
                .map(Enum::name)
                .toList();
    }

    @Override
    protected List<IParameter> resolveParameters(CodeObjectRegistry registry) {
        return getWrappedObject().getParameters()
                .stream()
                .map(parameter -> registry.getRegister(IParameter.class).getOrCreate(
                        QualifiedNameFactory.fromCtElement(parameter),
                        () -> new WrappedCtParameter(parameter, this)))
                .toList();
    }

    @Override
    protected List<IType> resolveExceptions(CodeObjectRegistry registry) {
        return getWrappedObject().getThrownTypes()
                .stream()
                .map(CtTypeReference::getTypeDeclaration)
                .filter(Objects::nonNull)
                .map(t -> registry.getRegister(IType.class).getOrCreate(
                        QualifiedNameFactory.fromCtElement(t),
                        () -> new WrappedCtType(t)))
                .toList();
    }

    @Override
    protected Optional<IType> resolveReturnType(CodeObjectRegistry registry) {
        if (getWrappedObject().getType() != null && getWrappedObject().getType().getTypeDeclaration() != null) {
            return Optional.of(
                    registry.getRegister(IType.class).getOrCreate(
                            QualifiedNameFactory.fromCtElement(getWrappedObject().getType().getTypeDeclaration()),
                            () -> new WrappedCtType(getWrappedObject().getType().getTypeDeclaration()))
            );
        }
        return Optional.empty();
    }

    @Override
    protected String resolveSignature(CodeObjectRegistry registry) {
        return getWrappedObject().getSignature();
    }

    @Override
    protected String resolveMethodBody(CodeObjectRegistry registry) {
        return getWrappedObject().getBody() != null ? getWrappedObject().getBody().toString() : "";
    }

    @Override
    protected String resolveDocumentation(CodeObjectRegistry registry) {
        return getWrappedObject().getDocComment();
    }

    @Override
    protected List<IMethod> resolveReferencedMethods(CodeObjectRegistry registry) {
        return getWrappedObject()
                .getElements(new TypeFilter<>(CtInvocation.class))
                .stream()
                .map(ctInvocation -> {
                    // Catch exceptions in spoon when the executable cannot be resolved
                    // This happens for types like '$$2'
                    try {
                        return ctInvocation.getExecutable().getDeclaration();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(decl -> registry.getRegister(IMethod.class).get(QualifiedNameFactory.fromCtElement(decl)))
                .toList();
    }

    @Override
    protected List<IField> resolveReferencedFields(CodeObjectRegistry registry) {
        if (getWrappedObject().getBody() == null) {
            return List.of();
        }

        return getWrappedObject()
                .getBody()
                .filterChildren(CtFieldAccess.class::isInstance)
                .list(CtFieldAccess.class)
                .stream()
                .filter(ctFieldAccess -> !(ctFieldAccess.getTarget() instanceof CtFieldAccess<?>)) // Exclude chained field accesses
                .map(ctFieldAccess -> {
                    try {
                        return ctFieldAccess.getVariable().getDeclaration();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(decl -> registry.getRegister(IField.class).get(QualifiedNameFactory.fromCtElement(decl)))
                .toList();
    }
}
