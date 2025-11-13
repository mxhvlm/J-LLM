package org.example.impl.datamodel.api.code.wrapper;

import java.util.List;

public interface IPackage extends INamedElement {
    List<IType> getTypes();

    List<IPackage> getSubPackages();
}