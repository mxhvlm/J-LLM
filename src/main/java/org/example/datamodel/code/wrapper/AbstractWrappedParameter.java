package org.example.datamodel.code.wrapper;

import org.example.datamodel.code.QualifiedName;
import org.neo4j.driver.internal.shaded.reactor.util.annotation.Nullable;
import spoon.reflect.declaration.CtParameter;

import java.util.Optional;

public abstract class AbstractWrappedParameter extends AbstractWrappedElement<CtParameter<?>> implements IParameter {
    protected @Nullable IType _type;

    public AbstractWrappedParameter(CtParameter<?> wrappedObject, INamedElement parent, QualifiedName qualifiedName) {
        super(wrappedObject, qualifiedName);
        this._parent = parent;
    }

    @Override
    public void resolve(CodeObjectRegistry registry) {
        super.resolve(registry);
        _type = resolveType(registry).orElse(null);
    }

    @Override
    public Optional<IType> getType() {
        return Optional.ofNullable(_type);
    }

    // Hook methods to resolve lib-specific resolution logic
    protected abstract Optional<IType> resolveType(CodeObjectRegistry registry);
}
