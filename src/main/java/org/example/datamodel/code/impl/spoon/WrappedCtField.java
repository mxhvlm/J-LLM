package org.example.datamodel.code.impl.spoon;

import org.example.datamodel.code.wrapper.IField;
import org.example.datamodel.code.wrapper.IType;
import spoon.reflect.declaration.CtField;

import java.util.List;

public class WrappedCtField implements IField {
    private final CtField<?> _field;
    private final IType _parent;

    public static final char QUALIFIED_NAME_SEPARATOR = '#';

    public WrappedCtField(CtField<?> field, IType parent) {
        this._field = field;
        this._parent = parent;
    }

    @Override
    public List<String> getModifiers() {
        return _field.getModifiers()
                .stream()
                .map(Enum::name)
                .toList();
    }

    @Override
    public String getName() {
        return _field.getSimpleName();
    }

    @Override
    public String getQualifiedName() {
        return _parent.getQualifiedName() + QUALIFIED_NAME_SEPARATOR + _field.getSimpleName();
    }

    @Override
    public List<String> getQualifiedNameParts() {
        List<String> parts = _parent.getQualifiedNameParts();
        parts.add("" + QUALIFIED_NAME_SEPARATOR);
        parts.add(_field.getSimpleName());
        return parts;
    }

    @Override
    public char getSeparator() {
        return QUALIFIED_NAME_SEPARATOR;
    }
}
