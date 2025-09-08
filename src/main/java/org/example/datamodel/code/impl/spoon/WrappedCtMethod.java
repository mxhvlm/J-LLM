package org.example.datamodel.code.impl.spoon;

import org.apache.commons.lang3.NotImplementedException;
import org.example.datamodel.code.wrapper.IMethod;
import org.example.datamodel.code.wrapper.INamedElement;
import org.example.datamodel.code.wrapper.IParameter;
import org.example.datamodel.code.wrapper.IType;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.stream.Collectors;

public class WrappedCtMethod implements IMethod {
    private final CtMethod<?> _method;
    private final INamedElement _parent;

    public WrappedCtMethod(CtMethod<?> method, INamedElement parent) {
        this._method = method;
        this._parent = parent;
    }

    @Override
    public List<String> getModifiers() {
        return _method.getModifiers()
                .stream()
                .map(Enum::name)
                .toList();
    }

    @Override
    public List<IParameter> getParameters() {
        return _method.getParameters()
                .stream()
                .map(parameter -> new WrappedCtParameter(parameter, this))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<IType> getExceptions() {
        return _method.getThrownTypes()
                .stream()
                .map(t -> new WrappedCtType(t.getDeclaration(), this))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public IType getReturnType() {
        throw new NotImplementedException("TODO: Look for how to get method return type in Spoon.");
    }

    @Override
    public String getName() {
        return _method.getSimpleName();
    }

    @Override
    public String getQualifiedName() {
        return _parent.getQualifiedName() + getSeparator() + _method.getSimpleName();
    }

    @Override
    public List<String> getQualifiedNameParts() {
        return List.of();
    }

    @Override
    public char getSeparator() {
        return '#';
    }
}
