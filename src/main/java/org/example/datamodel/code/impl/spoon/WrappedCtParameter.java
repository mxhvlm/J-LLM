package org.example.datamodel.code.impl.spoon;

import org.example.datamodel.code.wrapper.INamedElement;
import org.example.datamodel.code.wrapper.IParameter;
import org.example.datamodel.code.wrapper.IType;
import spoon.reflect.declaration.CtParameter;

import java.util.List;

public class WrappedCtParameter implements IParameter {
    private final CtParameter<?> _parameter;
    private final INamedElement _parent;

    public WrappedCtParameter(CtParameter<?> parameter, INamedElement parent) {
        this._parameter = parameter;
        this._parent = parent;
    }

    @Override
    public IType getType() {
        return new WrappedCtType(_parameter.getType().getTypeDeclaration(), this);
    }

    @Override
    public String getName() {
        return _parameter.getSimpleName();
    }

    @Override
    public String getQualifiedName() {
        return _parent.getQualifiedName() + getSeparator() + _parameter.getSimpleName();
    }

    @Override
    public List<String> getQualifiedNameParts() {
        List<String> parts = _parent.getQualifiedNameParts();
        parts.add("" + getSeparator());
        parts.add(_parameter.getSimpleName());
        return parts;
    }

    @Override
    public char getSeparator() {
        return '-';
    }
}
