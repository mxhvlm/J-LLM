package org.example.datamodel.api.code.wrapper;

import org.example.datamodel.impl.code.wrapper.CodeObjectRegistry;

import java.util.List;
import java.util.Optional;

/**
 * Wrapper interface for a method in the code model, providing access to its properties and relationships.
 * This interface is independent of the specific library / implementation used to parse the code.
 */
public interface IMethod extends INamedElement {
    /**
     * Gets the modifiers of the method.
     * @return A String list of modifiers.
     */
    List<String> getModifiers();

    /**
     * Gets the parameters of the method.
     * @return The parameters. If there are no parameters, an empty list is returned.
     */
    List<IParameter> getParameters();

    /**
     * Gets the exceptions thrown by the method.
     * @return The exceptions. If there are no exceptions, an empty list is returned.
     */
    List<IType> getExceptions();

    /**
     * Gets the return type of the method.
     * @return The return type. If the method has no return type (i.e., void), an empty Optional is returned.
     */
    Optional<IType> getReturnType();

    /**
     * Gets the methods referenced within the body of this method.
     * @return The referenced methods. If there are no referenced methods, an empty list is returned.
     * Returns static references only.
     */
    List<IMethod> getReferencedMethods();

    /**
     * Gets the fields referenced within the body of this method.
     * @return The referenced fields. If there are no referenced fields, an empty list is returned.
     */
    List<IField> getReferencedFields();

    /**
     * Gets the type that declares this method.
     * @return The declaring type. If the method is not declared within a type, an empty Optional is returned.
     * If the declaring type is not on the classpath, an empty Optional is returned.
     */
    Optional<IType> getDeclaringType();

    /**
     * Gets the signature of the method, which includes the method name and parameter types.
     * @return The method signature.
     */
    String getSignature();

    /**
     * Gets the body of the method as a string.
     * @return The source code of the method body.
     */
    String getBody();

    /**
     * Gets the documentation comment associated with the method.
     * @return The documentation comment as a string.
     */
    String getDocumentation();

    /**
     * Resolve static references within the method using the provided registry.
     * This can't be done inside the INamedElement resolve method, as it requires all methods to be created first.
     * @param registry The code object registry to use for de-duplication
     */
    void resolveStaticRefs(CodeObjectRegistry registry);
}
