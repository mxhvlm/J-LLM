package com.mxhvlm.jllm.core.datamodel.api.code.wrapper;

public interface IResolvable {

    void resolve(ICodeObjectRegistry registry);

    boolean isResolved();
}
