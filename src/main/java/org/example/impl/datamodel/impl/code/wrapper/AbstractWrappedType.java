package org.example.impl.datamodel.impl.code.wrapper;

import org.example.impl.datamodel.api.code.IQualifiedName;
import org.example.impl.datamodel.api.code.wrapper.*;
import org.neo4j.driver.internal.shaded.reactor.util.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractWrappedType<T> extends AbstractWrappedElement<T> implements IType {
    protected static final Logger _LOGGER = LoggerFactory.getLogger(AbstractWrappedType.class);
    protected final List<IField> _fields;
    protected final List<IMethod> _methods;
    protected final List<IType> _innerTypes;
    protected final List<IType> _interfaces;
    protected final List<String> _modifiers;
    protected @Nullable IType _superType;
    protected EnumTypeKind _typeKind;

    public AbstractWrappedType(T wrappedObject, IQualifiedName qualifiedName) {
        super(wrappedObject, qualifiedName);
        _fields = new LinkedList<>();
        _methods = new LinkedList<>();
        _innerTypes = new LinkedList<>();
        _interfaces = new LinkedList<>();
        _modifiers = new LinkedList<>();
    }

    @Override
    public void resolve(ICodeObjectRegistry registry) {
        super.resolve(registry);

        _parent = resolveParent(registry).orElse(null);
        _superType = resolveSuperType(registry).orElse(null);
        _innerTypes.addAll(resolveInnerTypes(registry));
        _fields.addAll(resolveFields(registry));
        _methods.addAll(resolveMethods(registry));
        _interfaces.addAll(resolveInterfaces(registry));
        _modifiers.addAll(resolveModifiers(registry));
        _typeKind = resolveTypeKind(registry);
    }

    @Override
    public List<IField> getFields() {
        return _fields;
    }

    @Override
    public List<IMethod> getMethods() {
        return _methods;
    }

    @Override
    public List<IType> getInnerTypes() {
        return _innerTypes;
    }

    @Override
    public Optional<IPackage> getPackage() {
        return getParent()
                .flatMap(p -> {
                    if (p instanceof IPackage pkg) {
                        return Optional.of(pkg);
                    } else if (p instanceof IType type) {
                        return type.getPackage();
                    } else {
                        throw new IllegalStateException("Parent is neither package nor type: " + p);
                    }
                });
    }

    @Override
    public Optional<IType> getSuperClass() {
        return Optional.ofNullable(_superType);
    }

    @Override
    public List<IType> getInterfaces() {
        return _interfaces;
    }

    @Override
    public EnumTypeKind getTypeKind() {
        return _typeKind;
    }

    @Override
    public Collection<String> getModifiers() {
        return _modifiers;
    }

    // Hook methods providing lib-specific resolution logic
    protected abstract List<IField> resolveFields(ICodeObjectRegistry registry);

    protected abstract List<IMethod> resolveMethods(ICodeObjectRegistry registry);

    protected abstract List<IType> resolveInnerTypes(ICodeObjectRegistry registry);

    protected abstract Optional<IType> resolveSuperType(ICodeObjectRegistry registry);

    protected abstract List<IType> resolveInterfaces(ICodeObjectRegistry registry);

    protected abstract Optional<INamedElement> resolveParent(ICodeObjectRegistry registry);

    protected abstract EnumTypeKind resolveTypeKind(ICodeObjectRegistry registry);

    protected abstract Collection<String> resolveModifiers(ICodeObjectRegistry registry);
}
