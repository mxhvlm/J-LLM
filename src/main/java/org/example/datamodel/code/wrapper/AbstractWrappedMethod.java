package org.example.datamodel.code.wrapper;

import org.example.datamodel.code.QualifiedName;
import org.neo4j.driver.internal.shaded.reactor.util.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractWrappedMethod<T> extends AbstractWrappedElement<T> implements IMethod {
    protected final List<String> _modifiers;
    protected final List<IParameter> _parameters;
    protected final List<IType> _exceptions;
    protected final List<IMethod> _referencedMethods;
    protected final List<IField> _referencedFields;
    protected @Nullable IType _returnType;
    protected String _signature;
    protected String _methodBody;
    protected String _documentation;

    public AbstractWrappedMethod(T wrappedObject, INamedElement parent, QualifiedName qualifiedName) {
        super(wrappedObject, qualifiedName);
        _parent = parent;
        _modifiers = new LinkedList<>();
        _parameters = new LinkedList<>();
        _exceptions = new LinkedList<>();
        _referencedMethods = new LinkedList<>();
        _referencedFields = new LinkedList<>();
        _signature = "";
        _methodBody = "";
        _documentation = "";
    }

    @Override
    public void resolve(CodeObjectRegistry registry) {
        super.resolve(registry);
        
        _modifiers.addAll(resolveModifiers(registry));
        _parameters.addAll(resolveParameters(registry));
        _exceptions.addAll(resolveExceptions(registry));
        _returnType = resolveReturnType(registry).orElse(null);
        _signature = resolveSignature(registry);
        _methodBody = resolveMethodBody(registry);
        _documentation = resolveDocumentation(registry);
        _referencedFields.addAll(resolveReferencedFields(registry));
    }

    @Override
    public void resolveStaticRefs(CodeObjectRegistry registry) {
        _referencedMethods.addAll(resolveReferencedMethods(registry));
    }

    public List<String> getModifiers() {
        return _modifiers;
    }

    public List<IParameter> getParameters() {
        return _parameters;
    }

    public List<IType> getExceptions() {
        return _exceptions;
    }

    public Optional<IType> getReturnType() {
        return Optional.ofNullable(_returnType);
    }

    @Override
    public List<IMethod> getReferencedMethods() {
        return _referencedMethods;
    }

    @Override
    public List<IField> getReferencedFields() {
        return _referencedFields;
    }

    @Override
    public Optional<IType> getDeclaringType() {
        return Optional.ofNullable(_parent)
                .filter(p -> p instanceof IType)
                .map(p -> (IType) p);
    }

    @Override
    public String getBody() {
        return _methodBody;
    }

    @Override
    public String getSignature() {
        return _signature;
    }

    @Override
    public String getDocumentation() {
        return "";
    }

    // Hook methods providing the lib-specific resolution logic
    protected abstract List<String> resolveModifiers(CodeObjectRegistry registry);
    protected abstract List<IParameter> resolveParameters(CodeObjectRegistry registry);
    protected abstract List<IType> resolveExceptions(CodeObjectRegistry registry);
    protected abstract Optional<IType> resolveReturnType(CodeObjectRegistry registry);
    protected abstract String resolveSignature(CodeObjectRegistry registry);
    protected abstract String resolveMethodBody(CodeObjectRegistry registry);
    protected abstract String resolveDocumentation(CodeObjectRegistry registry);
    protected abstract List<IMethod> resolveReferencedMethods(CodeObjectRegistry registry);
    protected abstract List<IField> resolveReferencedFields(CodeObjectRegistry registry);
}