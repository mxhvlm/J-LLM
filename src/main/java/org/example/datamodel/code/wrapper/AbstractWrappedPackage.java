package org.example.datamodel.code.wrapper;

import org.example.datamodel.code.QualifiedName;
import org.neo4j.driver.internal.shaded.reactor.util.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractWrappedPackage<T> extends AbstractWrappedElement<T> implements IPackage {
    protected final List<IType> _subTypes;
    protected final List<IPackage> _subPackages;

    public AbstractWrappedPackage(T wrappedObject, QualifiedName qualifiedName) {
        super(wrappedObject, qualifiedName);
        _subTypes = new LinkedList<>();
        _subPackages = new LinkedList<>();
    }

    @Override
    public void resolve(CodeObjectRegistry registry) {
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
    protected abstract List<IType> resolveSubTypes(CodeObjectRegistry registry);
    protected abstract List<IPackage> resolveSubPackages(CodeObjectRegistry registry);
}
