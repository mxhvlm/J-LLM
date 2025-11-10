package org.example.datamodel.code.impl.spoon;

import org.example.datamodel.code.wrapper.AbstractWrappedElement;
import org.example.datamodel.code.wrapper.CodeObjectRegistry;
import org.example.datamodel.code.wrapper.IField;
import org.example.datamodel.code.wrapper.IType;
import org.neo4j.driver.internal.shaded.reactor.util.annotation.Nullable;
import spoon.reflect.declaration.CtField;

import java.util.List;
import java.util.Optional;

public class WrappedCtField extends AbstractWrappedElement<CtField<?>> implements IField {
    private @Nullable IType _type;

    public WrappedCtField(CtField<?> field, IType parent) {
        super(field, QualifiedNameFactory.fromCtElement(field));
        this._parent = parent;
    }

    @Override
    public List<String> getModifiers() {
        return getWrappedObject().getModifiers()
                .stream()
                .map(Enum::name)
                .toList();
    }

    @Override
    public Optional<IType> getType() {
        return Optional.ofNullable(_type);
    }

    @Override
    public Optional<IType> getDeclaringType() {
        return Optional.ofNullable(_parent)
                .filter(p -> p instanceof IType)
                .map(p -> (IType) p);
    }

    @Override
    public void resolve(CodeObjectRegistry registry) {
        super.resolve(registry);

        if (getWrappedObject().getType().getTypeDeclaration() != null) {
            _type = registry.getRegister(IType.class).getOrCreate(
                    QualifiedNameFactory.fromCtElement(getWrappedObject().getType().getTypeDeclaration()),
                    () -> new WrappedCtType(getWrappedObject().getType().getTypeDeclaration()));
        }
    }
}
