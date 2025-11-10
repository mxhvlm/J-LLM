package org.example.datamodel.code.wrapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IType extends INamedElement {
    /**
     * Returns the fields declared within this type.
     * @return A list of fields, or an empty list if none exist.
     */
    List<IField> getFields();

    /**
     * Returns the methods declared within this type.
     * @return A list of methods, or an empty list if none exist.
     */
    List<IMethod> getMethods();

    /**
     * Returns the inner types declared within this type.
     * @return A list of inner types, or an empty list if none exist.
     */
    List<IType> getInnerTypes();

    /**
     * Returns the kind of this type (e.g., class, interface, enum, annotation).
     * @return
     */
    EnumTypeKind getTypeKind();

    /**
     * Returns the modifiers of this type (e.g., "public", "abstract", "final").
     * @return A collection of modifier strings.
     */
    Collection<String> getModifiers();

    /**
     * Returns the package this type belongs to, if any.
     * @return The package of this type, or an empty Optional if it has no package, i.e. it is an anonymous or local type.
     */
    Optional<IPackage> getPackage();

    /**
     * Returns the super class of this type, if any. If this type represents an interface or a class without a superclass (like java.lang.Object), an empty Optional is returned.
     * If the super type is not on the classpath, an empty Optional is also returned.
     * @return The super class of this type, or an empty Optional if none exists or is not on the classpath.
     */
    Optional<IType> getSuperClass();

    /**
     * Returns a list of interfaces implemented by this type. If this type represents an interface, the returned interfaces are the ones it extends.
     * If no interfaces are implemented or extended, an empty list is returned.
     * If an interface is not on the classpath, it is simply omitted from the list.
     * @return The list of interfaces implemented or extended by this type.
     */
    List<IType> getInterfaces();
}
