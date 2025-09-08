package org.example.datamodel.code.impl.spoon;

import org.example.datamodel.code.wrapper.IPackage;
import org.example.datamodel.code.wrapper.IType;
import spoon.reflect.declaration.CtPackage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WrappedCtPackage implements IPackage {
    private final CtPackage _package;

    public WrappedCtPackage(CtPackage _package) {
        this._package = _package;
    }

    @Override
    public List<IType> getTypes() {
        return _package.getTypes()
                .stream()
                .map(t -> new WrappedCtType(t, this))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<IPackage> getSubPackages() {
        return _package.getPackages()
                .stream()
                .map(WrappedCtPackage::new)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public String getName() {
        return _package.getSimpleName();
    }

    @Override
    public String getQualifiedName() {
        return _package.getQualifiedName();
    }

    @Override
    public List<String> getQualifiedNameParts() {
        return Arrays.stream(_package.getQualifiedName().split("\\.")).toList();
    }

    @Override
    public char getSeparator() {
        return '.';
    }
}
