package org.example.datamodel.code.wrapper;

import java.util.List;

public interface IMethod extends INamedElement {
    List<String> getModifiers();
    List<IParameter> getParameters();
    List<IType> getExceptions();
    IType getReturnType();
}
