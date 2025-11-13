package com.mxhvlm.jllm.core.datamodel.impl.code.wrapper;

import com.mxhvlm.jllm.core.datamodel.api.code.IQualifiedName;
import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.ICodeObjectRegistry;
import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.INamedElement;
import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.IParameter;
import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.IType;
import org.neo4j.driver.internal.shaded.reactor.util.annotation.Nullable;
import spoon.reflect.declaration.CtParameter;

import java.util.Optional;

public abstract class AbstractWrappedParameter extends AbstractWrappedElement<CtParameter<?>> implements IParameter {
    protected @Nullable IType _type;

    public AbstractWrappedParameter(CtParameter<?> wrappedObject, INamedElement parent, IQualifiedName qualifiedName) {
        super(wrappedObject, qualifiedName);
        this._parent = parent;
    }

    @Override
    public void resolve(ICodeObjectRegistry registry) {
        super.resolve(registry);
        _type = resolveType(registry).orElse(null);
    }

    @Override
    public Optional<IType> getType() {
        return Optional.ofNullable(_type);
    }

    // Hook methods to resolve lib-specific resolution logic
    protected abstract Optional<IType> resolveType(ICodeObjectRegistry registry);
}
