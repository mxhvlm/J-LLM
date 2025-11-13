package com.mxhvlm.jllm.core.integration.api;

import java.util.Optional;
import java.util.function.Function;

public interface IApiResponse<T> {
    boolean isSuccess();

    boolean isFailure();

    Optional<String> getErrorMessage();

    Optional<T> getResponse();

    <U> U map(Function<T, U> successHandler, Function<String, U> errorHandler);
}
