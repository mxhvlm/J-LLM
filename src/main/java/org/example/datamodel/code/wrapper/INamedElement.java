package org.example.datamodel.code.wrapper;

import java.util.List;

/**
 * Base class for all elements withing a code model.
 */
public interface INamedElement {
    /**
     * Get the name of the element.
     * @return the name of the element
     */
    String getName();

    /**
     * Get the qualified name of the element, which includes all the details fully qualifying the element's name.
     * @ensures The qualified name is unique within the context of the code model.
     * @return the qualified name of the element
     */
    String getQualifiedName();

    /**
     * Get the parts of the qualified name as a stream.
     * This can be useful for iterating over the components of the qualified name.
     * This includes split characters like '.' or '$' that are used to separate the parts of the qualified name.
     * @ensures The concatenation of the parts will be unique within the context of the code model.
     * @return a stream of strings representing the parts of the qualified name
     */
    List<String> getQualifiedNameParts();

    /**
     * Get the separator character used in the qualified name.
     * This is typically a '.' for package names or a '$' for inner classes or "#" for type members.
     * @return the separator character used in the qualified name
     */
    char getSeparator();
}
