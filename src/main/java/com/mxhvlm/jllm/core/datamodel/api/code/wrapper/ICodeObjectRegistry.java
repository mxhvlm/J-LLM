package com.mxhvlm.jllm.core.datamodel.api.code.wrapper;

import java.util.stream.Stream;

public interface ICodeObjectRegistry {
    <T, U> void createRegister(Class<T> wrapperClass, Class<U> libraryClass);

    @SuppressWarnings("unchecked")
    <T extends INamedElement> ICodeObjectRegister<T> getRegister(Class<T> clazz);

    Stream<INamedElement> getAllObjects();
}
