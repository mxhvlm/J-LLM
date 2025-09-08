package org.example.datamodel.code.wrapper;

import java.util.List;

public interface IType extends INamedElement {
    List<IField> getFields();
    List<IMethod> getMethods();
    List<IType> getInnerTypes();
}
