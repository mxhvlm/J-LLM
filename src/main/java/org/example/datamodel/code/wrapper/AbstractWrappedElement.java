package org.example.datamodel.code.wrapper;

import org.example.datamodel.code.QualifiedName;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractWrappedElement<T> implements IWrappedObject<T>, INamedElement {
    protected final T _wrappedObject;
    protected final QualifiedName _qualifiedName;
    protected INamedElement _parent;
    private boolean _isResolved;

    protected static final Logger _LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractWrappedElement.class);

    public AbstractWrappedElement(T wrappedObject, QualifiedName qualifiedName) {
        _wrappedObject = Objects.requireNonNull(wrappedObject);
        _qualifiedName = Objects.requireNonNull(qualifiedName);
        _isResolved = false;
    }

    @Override
    public void resolve(CodeObjectRegistry registry) {
        if (_isResolved) {
            throw new IllegalStateException("Element is already resolved: " + getName());
        }
        _isResolved = true;
    }

    public T getWrappedObject() {
        return _wrappedObject;
    }

    @Override
    public QualifiedName getName() {
        return _qualifiedName;
    }

    @Override
    public Optional<INamedElement> getParent() {
        return Optional.ofNullable(_parent);
    }

    @Override
    public boolean isResolved() {
        return _isResolved;
    }

    @Override
    public int hashCode() {
        return _qualifiedName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractWrappedElement<?> other &&
               this._qualifiedName.equals(other._qualifiedName);
    }
}
