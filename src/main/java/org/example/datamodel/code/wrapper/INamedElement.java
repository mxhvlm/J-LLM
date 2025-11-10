package org.example.datamodel.code.wrapper;

import org.example.datamodel.code.QualifiedName;

import java.util.Optional;

/**
 * Base class for all elements withing a code model.
 */
public interface INamedElement extends IResolvable {
    /**
     * Get the qualified name of the element, which includes all the details fully qualifying the element's name.
     * @ensures The qualified name is unique within the context of the code model.
     * @return the qualified name of the element
     */
    QualifiedName getName();

    /**
     * Get the parent element in the code model hierarchy.
     * @return The parent element, or an empty Optional if there is no parent.
     * This can be the case for the top-level element or anonymous elements.
     */
    Optional<INamedElement> getParent();


}
