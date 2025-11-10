package org.example.datamodel.code.wrapper;

import java.util.Optional;

public interface IParameter extends INamedElement {
    Optional<IType> getType();
}
