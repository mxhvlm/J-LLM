package com.mxhvlm.jllm.core.datamodel.api.code.wrapper;

import java.util.Optional;

public interface IParameter extends INamedElement {
    Optional<IType> getType();
}
