package org.example.datamodel.code.impl.spoon;

import org.example.datamodel.code.wrapper.*;
import spoon.reflect.declaration.CtParameter;

import java.util.Optional;

public class WrappedCtParameter extends AbstractWrappedParameter {
    public WrappedCtParameter(CtParameter<?> parameter, INamedElement parent) {
        super(parameter, parent, QualifiedNameFactory.fromCtElement(parameter));
    }

    @Override
    protected Optional<IType> resolveType(CodeObjectRegistry registry) {
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
