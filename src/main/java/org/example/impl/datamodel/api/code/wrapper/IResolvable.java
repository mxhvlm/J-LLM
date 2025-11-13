package org.example.impl.datamodel.api.code.wrapper;

public interface IResolvable {

    void resolve(ICodeObjectRegistry registry);

    boolean isResolved();
}
