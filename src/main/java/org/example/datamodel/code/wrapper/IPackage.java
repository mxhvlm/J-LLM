package org.example.datamodel.code.wrapper;

import java.util.List;

public interface IPackage extends INamedElement {
    List<IType> getTypes();
    List<IPackage> getSubPackages();
}
