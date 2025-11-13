package org.example.datamodel.api.code.wrapper;

import org.example.datamodel.impl.code.wrapper.CodeObjectRegistry;

public interface IResolvable {

    void resolve(CodeObjectRegistry registry);

    boolean isResolved();
}
