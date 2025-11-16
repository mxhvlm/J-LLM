package com.mxhvlm.jllm.core.datamodel.impl.code.spoon;

import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.ICodeObjectRegistry;
import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.INamedElement;
import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.IType;
import com.mxhvlm.jllm.core.datamodel.impl.code.wrapper.AbstractWrappedParameter;
import spoon.reflect.declaration.CtParameter;

import java.util.Optional;

public class WrappedCtParameter extends AbstractWrappedParameter {
    public WrappedCtParameter(CtParameter<?> parameter, INamedElement parent) {
        super(parameter, parent, QualifiedNameFactory.fromCtElement(parameter));
    }

    @Override
    protected Optional<IType> resolveType(ICodeObjectRegistry registry) {
        if (getWrappedObject().getType() == null || getWrappedObject().getType().getTypeDeclaration() == null) {
            return Optional.empty();
        }
        return Optional.of(
                registry.getRegister(IType.class).getOrCreate(
                        QualifiedNameFactory.fromCtElement(getWrappedObject()),
                        () -> new WrappedCtType(getWrappedObject().getType().getTypeDeclaration()))
        );
    }
}
