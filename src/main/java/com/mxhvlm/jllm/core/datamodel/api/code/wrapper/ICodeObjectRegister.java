package com.mxhvlm.jllm.core.datamodel.api.code.wrapper;

import com.mxhvlm.jllm.core.datamodel.api.code.IQualifiedName;

import java.util.List;
import java.util.function.Supplier;

public interface ICodeObjectRegister<T extends INamedElement> {
    T getOrCreate(IQualifiedName name, Supplier<T> supplier);

    T get(IQualifiedName name);

    List<T> getAll();
}
