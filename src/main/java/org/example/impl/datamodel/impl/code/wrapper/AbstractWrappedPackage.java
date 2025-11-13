package org.example.impl.datamodel.impl.code.wrapper;

import org.example.impl.datamodel.api.code.IQualifiedName;
import org.example.impl.datamodel.api.code.wrapper.ICodeObjectRegistry;
import org.example.impl.datamodel.api.code.wrapper.IPackage;
import org.example.impl.datamodel.api.code.wrapper.IType;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractWrappedPackage<T> extends AbstractWrappedElement<T> implements IPackage {
    protected final List<IType> _subTypes;
    protected final List<IPackage> _subPackages;

    public AbstractWrappedPackage(T wrappedObject, IQualifiedName qualifiedName) {
        super(wrappedObject, qualifiedName);
        _subTypes = new LinkedList<>();
        _subPackages = new LinkedList<>();
    }

    @Override
    public void resolve(ICodeObjectRegistry registry) {
        super.resolve(registry);

        _subTypes.addAll(resolveSubTypes(registry));
        _subPackages.addAll(resolveSubPackages(registry));
    }

    public List<IType> getTypes() {
        return _subTypes;
    }

    public List<IPackage> getSubPackages() {
        return _subPackages;
    }

    // Hook methods providing lib-specific resolution logic
    protected abstract List<IType> resolveSubTypes(ICodeObjectRegistry registry);

    protected abstract List<IPackage> resolveSubPackages(ICodeObjectRegistry registry);
}
