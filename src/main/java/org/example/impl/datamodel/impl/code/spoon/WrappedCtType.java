package org.example.impl.datamodel.impl.code.spoon;

import org.example.impl.datamodel.api.code.wrapper.*;
import org.example.impl.datamodel.impl.code.wrapper.AbstractWrappedType;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WrappedCtType extends AbstractWrappedType<CtType<?>> {

    public WrappedCtType(CtType<?> type) {
        super(Objects.requireNonNull(type), QualifiedNameFactory.fromCtElement(type));
    }

    @Override
    protected List<IField> resolveFields(ICodeObjectRegistry registry) {
        return getWrappedObject().getFields()
                .stream()
                .map(field -> registry.getRegister(IField.class).getOrCreate(
                        QualifiedNameFactory.fromCtElement(field),
                        () -> new WrappedCtField(field, this)))
                .toList();
    }

    @Override
    protected List<IMethod> resolveMethods(ICodeObjectRegistry registry) {
        return getWrappedObject().getMethods()
                .stream()
                .map(method -> registry.getRegister(IMethod.class).getOrCreate(
                        QualifiedNameFactory.fromCtElement(method),
                        () -> new WrappedCtMethod<>(method, this)))
                .toList();
    }

    @Override
    protected List<IType> resolveInnerTypes(ICodeObjectRegistry registry) {
        return getWrappedObject().getNestedTypes()
                .stream()
                .map(type -> registry.getRegister(IType.class)
                        .getOrCreate(QualifiedNameFactory.fromCtElement(type), () -> new WrappedCtType(type)))
                .toList();
    }

    @Override
    protected Optional<IType> resolveSuperType(ICodeObjectRegistry registry) {
        if (getWrappedObject().getSuperclass() != null && getWrappedObject().getSuperclass().getTypeDeclaration() != null) {
            return Optional.of(
                    registry.getRegister(IType.class).getOrCreate(
                            QualifiedNameFactory.fromCtElement(getWrappedObject().getSuperclass().getTypeDeclaration()),
                            () -> new WrappedCtType(getWrappedObject().getSuperclass().getTypeDeclaration()))
            );
        } else {
//            _LOGGER.warn("Could not resolve super type for: " + getName() + ", " + getWrappedObject().getSuperclass());
            return Optional.empty();
        }
    }

    @Override
    protected List<IType> resolveInterfaces(ICodeObjectRegistry registry) {
        return getWrappedObject().getSuperInterfaces().stream().map(interfaceRef -> {
            if (interfaceRef.getTypeDeclaration() != null) {
                return registry.getRegister(IType.class).getOrCreate(
                        QualifiedNameFactory.fromCtElement(interfaceRef.getTypeDeclaration()),
                        () -> new WrappedCtType(interfaceRef.getTypeDeclaration()));
            } else {
//                _LOGGER.warn("Could not resolve interface type for: " + getName() + ", " + interfaceRef);
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    @Override
    protected Optional<INamedElement> resolveParent(ICodeObjectRegistry registry) {
        if (!_wrappedObject.isParentInitialized()) {
            return Optional.empty();
        }

        CtElement parent = getWrappedObject().getParent();
        if (parent instanceof CtPackage parentPackage) {
            return Optional.of(registry.getRegister(IPackage.class).getOrCreate(
                    QualifiedNameFactory.fromCtElement(parentPackage),
                    () -> new WrappedCtPackage((CtPackage) parent)));
        } else if (parent instanceof CtType<?> parentType) {
            return Optional.of(registry.getRegister(IType.class).getOrCreate(
                    QualifiedNameFactory.fromCtElement(parentType),
                    () -> new WrappedCtType((CtType<?>) parent)));
        } else {
//            _LOGGER.warn("Could not resolve type parent for: {}, parent: {}", getName(), parent);
            return Optional.empty();
        }
    }

    @Override
    protected EnumTypeKind resolveTypeKind(ICodeObjectRegistry registry) {
        if (getWrappedObject().isClass()) return EnumTypeKind.CLASS;
        if (getWrappedObject().isInterface()) return EnumTypeKind.INTERFACE;
        if (getWrappedObject().isEnum()) return EnumTypeKind.ENUM;
        if (getWrappedObject().isAnnotationType()) return EnumTypeKind.ANNOTATION;
        return EnumTypeKind.UNKNOWN;
    }

    @Override
    protected Collection<String> resolveModifiers(ICodeObjectRegistry registry) {
        return getWrappedObject().getModifiers()
                .stream()
                .map(Enum::name)
                .toList();
    }
}
