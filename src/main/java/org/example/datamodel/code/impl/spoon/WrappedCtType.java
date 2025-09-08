package org.example.datamodel.code.impl.spoon;

import org.example.datamodel.code.wrapper.IField;
import org.example.datamodel.code.wrapper.IMethod;
import org.example.datamodel.code.wrapper.INamedElement;
import org.example.datamodel.code.wrapper.IType;
import spoon.reflect.declaration.CtType;

import java.util.List;
import java.util.stream.Collectors;

public class WrappedCtType implements IType {
    private final CtType<?> _type;
    private final INamedElement _parent;

    public WrappedCtType(CtType<?> type, INamedElement parent) {
        this._type = type;
        this._parent = parent;
    }

    @Override
    public List<IField> getFields() {
        return _type.getFields()
                .stream()
                .map(field -> new WrappedCtField(field, this))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<IMethod> getMethods() {
            return _type.getMethods()
                    .stream()
                    .map(method -> new WrappedCtMethod(method, this))
                    .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<IType> getInnerTypes() {
        return _type.getNestedTypes()
                .stream()
                .map(t -> new WrappedCtType(t, this))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public String getName() {
        return _type.getSimpleName();
    }

    @Override
    public String getQualifiedName() {
        return _type.getQualifiedName();
    }

    @Override
    public List<String> getQualifiedNameParts() {
        return List.of();
    }

    @Override
    public char getSeparator() {
        return '$';
    }
}
