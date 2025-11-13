package com.mxhvlm.jllm.core.datamodel.api.code.wrapper;

import java.util.List;

public interface IPackage extends INamedElement {
    List<IType> getTypes();

    List<IPackage> getSubPackages();
}