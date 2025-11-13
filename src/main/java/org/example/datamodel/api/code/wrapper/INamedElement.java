package org.example.datamodel.api.code.wrapper;

import org.example.datamodel.api.code.IQualifiedName;

import java.util.Optional;

/**
 * Base class for all elements withing a code model.
 */
public interface INamedElement extends IResolvable {
    /**
     * Get the qualified name of the element, which includes all the details fully qualifying the element's name.
     *
     * @return the qualified name of the element
     * @ensures The qualified name is unique within the context of the code model.
     */
    IQualifiedName getName();

    /**
     * Get the parent element in the code model hierarchy.
     *
     * @return The parent element, or an empty Optional if there is no parent.
     * This can be the case for the top-level element or anonymous elements.
     */
    Optional<INamedElement> getParent();


}
