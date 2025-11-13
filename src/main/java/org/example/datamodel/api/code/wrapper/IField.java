package org.example.datamodel.api.code.wrapper;

import java.util.List;
import java.util.Optional;

public interface IField extends INamedElement {
    List<String> getModifiers();
    Optional<IType> getType();
    Optional<IType> getDeclaringType();
}
