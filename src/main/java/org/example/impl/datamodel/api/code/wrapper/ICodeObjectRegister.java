package org.example.impl.datamodel.api.code.wrapper;

import org.example.impl.datamodel.api.code.IQualifiedName;

import java.util.List;
import java.util.function.Supplier;

public interface ICodeObjectRegister<T extends INamedElement> {
    T getOrCreate(IQualifiedName name, Supplier<T> supplier);

    T get(IQualifiedName name);

    List<T> getAll();
}
